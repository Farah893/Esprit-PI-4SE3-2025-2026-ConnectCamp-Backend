package tn.esprit.projetintegre.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.config.InvoiceConfiguration;
import tn.esprit.projetintegre.dto.response.InvoiceDTO;
import tn.esprit.projetintegre.dto.response.InvoiceLineItemDTO;
import tn.esprit.projetintegre.entities.Invoice;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.repositories.InvoiceRepository;
import tn.esprit.projetintegre.repositories.OrderRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final InvoiceConfiguration invoiceConfig;

    private static final String UPLOAD_DIR = "uploads/invoices/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Couleurs
    private static final DeviceRgb COLOR_DARK    = new DeviceRgb(0x33, 0x33, 0x33);
    private static final DeviceRgb COLOR_BLUE    = new DeviceRgb(0x00, 0x66, 0xCC);
    private static final DeviceRgb COLOR_LIGHT   = new DeviceRgb(0xF8, 0xF8, 0xF8);
    private static final DeviceRgb COLOR_GREEN   = new DeviceRgb(0x27, 0xAE, 0x60);
    private static final DeviceRgb COLOR_ORANGE  = new DeviceRgb(0xE6, 0x7E, 0x22);
    private static final DeviceRgb COLOR_RED     = new DeviceRgb(0xC0, 0x39, 0x2B);
    private static final DeviceRgb COLOR_TOTAL   = new DeviceRgb(0xEE, 0xF2, 0xFF);

    // =========================================================
    // MÉTHODE PRINCIPALE
    // =========================================================

    @Transactional
    public InvoiceDTO generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable: " + orderId));

        List<Invoice> existing = invoiceRepository.findByOrderId(orderId);
        if (!existing.isEmpty()) {
            log.info("Facture existante trouvée: {}", existing.get(0).getInvoiceNumber());
            return toDTO(existing.get(0));
        }

        Invoice invoice = createInvoice(order);
        invoice = invoiceRepository.save(invoice);

        String qrPath = generateQrCode(invoice);
        String pdfPath = generatePdf(invoice, order, qrPath);
        invoice.setPdfUrl(pdfPath);
        invoice = invoiceRepository.save(invoice);

        log.info("Facture créée: {}", invoice.getInvoiceNumber());
        return toDTO(invoice);
    }

    // =========================================================
    // CRÉATION ENTITÉ INVOICE
    // =========================================================

    private Invoice createInvoice(Order order) {
        String invoiceNumber = generateInvoiceNumber(order);
        BigDecimal paidAmount = (order.getPaymentStatus() == PaymentStatus.COMPLETED)
                ? order.getTotalAmount() : BigDecimal.ZERO;

        return Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(invoiceConfig.getInvoiceValidityDays()))
                .subtotal(nvl(order.getSubtotal()))
                .taxAmount(nvl(order.getTaxAmount()))
                .discountAmount(nvl(order.getDiscountAmount()))
                .totalAmount(nvl(order.getTotalAmount()))
                .paidAmount(paidAmount)
                .status(order.getPaymentStatus())
                .billingAddress(buildBillingAddress(order))
                .notes(invoiceConfig.getLegalNotice())
                .user(order.getUser())
                .order(order)
                .build();
    }

    private String generateInvoiceNumber(Order order) {
        LocalDate ref = (order.getOrderedAt() != null)
                ? order.getOrderedAt().toLocalDate() : LocalDate.now();
        int year  = ref.getYear();
        int month = ref.getMonthValue();
        long count = invoiceRepository.countByMonth(year, month);
        return String.format("%s-%d-%02d-%05d",
                invoiceConfig.getInvoicePrefix(), year, month, count + 1);
    }

    // =========================================================
    // GÉNÉRATION PDF AVEC ITEXT 7
    // =========================================================

    private String generatePdf(Invoice invoice, Order order, String qrAbsolutePath) {
        try {
            Path pdfDir = Paths.get(UPLOAD_DIR + "pdf/");
            Files.createDirectories(pdfDir);

            String filename = invoice.getInvoiceNumber() + ".pdf";
            Path pdfPath = pdfDir.resolve(filename);

            List<InvoiceLineItemDTO> items = buildLineItems(order);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document doc = new Document(pdf, PageSize.A4);
                doc.setMargins(40, 40, 40, 40);

                PdfFont fontBold = PdfFontFactory.createFont("Helvetica-Bold");
                PdfFont fontNormal = PdfFontFactory.createFont("Helvetica");

                // ── En-tête ──────────────────────────────────────────
                addHeader(doc, invoice, fontBold, fontNormal);

                // ── Émetteur / Client ─────────────────────────────────
                addInfoSection(doc, invoice, order, fontBold, fontNormal);

                // ── Méta (dates, numéro commande) ─────────────────────
                addMetaSection(doc, invoice, order, fontBold, fontNormal);

                // ── Tableau des articles ──────────────────────────────
                addItemsTable(doc, items, order, fontBold, fontNormal);

                // ── Totaux ────────────────────────────────────────────
                addTotals(doc, invoice, order, fontBold, fontNormal);

                // ── Paiement ──────────────────────────────────────────
                addPaymentInfo(doc, invoice, order, fontBold, fontNormal);

                // ── QR Code ───────────────────────────────────────────
                addQrCode(doc, invoice, qrAbsolutePath, fontBold, fontNormal);

                // ── Footer ────────────────────────────────────────────
                addFooter(doc, fontNormal);

                doc.close();
                Files.write(pdfPath, baos.toByteArray());
            }

            log.info("PDF généré: {}", pdfPath.toAbsolutePath());
            return "/uploads/invoices/pdf/" + filename;

        } catch (Exception e) {
            log.error("Erreur génération PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur génération PDF: " + e.getMessage(), e);
        }
    }

    // ─── Sections PDF ────────────────────────────────────────

    private void addHeader(Document doc, Invoice invoice, PdfFont bold, PdfFont normal) {
        Paragraph title = new Paragraph("FACTURE")
                .setFont(bold).setFontSize(26)
                .setFontColor(COLOR_DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        doc.add(title);

        Paragraph num = new Paragraph("N° " + invoice.getInvoiceNumber())
                .setFont(normal).setFontSize(13)
                .setFontColor(new DeviceRgb(0x66, 0x66, 0x66))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16);
        doc.add(num);

        // Ligne séparatrice
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
                .setMarginBottom(16));
    }

    private void addInfoSection(Document doc, Invoice invoice, Order order,
                                PdfFont bold, PdfFont normal) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Émetteur
        Cell emetteur = new Cell().setBorder(Border.NO_BORDER)
                .add(infoBox(bold, normal,
                        "ÉMETTEUR",
                        invoiceConfig.getCompanyName(),
                        invoiceConfig.getCompanyAddress(),
                        invoiceConfig.getCompanyPostalCode() + " " + invoiceConfig.getCompanyCity(),
                        invoiceConfig.getCompanyCountry(),
                        "",
                        "SIRET : " + invoiceConfig.getSiret(),
                        "TVA : " + invoiceConfig.getTvaNumber(),
                        "Email : " + invoiceConfig.getCompanyEmail(),
                        "Tél : " + invoiceConfig.getCompanyPhone()
                ));

        // Client
        String customerName  = safe(order.getShippingName(), order.getUser() != null ? order.getUser().getName() : "");
        String customerEmail = order.getUser() != null ? order.getUser().getEmail() : "";
        String customerPhone = safe(order.getShippingPhone(), order.getUser() != null ? safeStr(order.getUser().getPhone()) : "");

        Cell client = new Cell().setBorder(Border.NO_BORDER)
                .add(infoBox(bold, normal,
                        "CLIENT",
                        customerName,
                        safeStr(invoice.getBillingAddress()).replace("\n", " "),
                        "",
                        "Email : " + customerEmail,
                        "Tél : " + customerPhone
                ));

        table.addCell(emetteur);
        table.addCell(client);
        doc.add(table);
    }

    private Div infoBox(PdfFont bold, PdfFont normal, String title, String... lines) {
        Div div = new Div()
                .setBackgroundColor(COLOR_LIGHT)
                .setPadding(12)
                .setBorderLeft(new SolidBorder(COLOR_BLUE, 4));

        div.add(new Paragraph(title).setFont(bold).setFontSize(10)
                .setFontColor(COLOR_BLUE).setMarginBottom(6));

        boolean first = true;
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            Paragraph p = new Paragraph(line).setFont(first ? bold : normal).setFontSize(9.5f).setMarginBottom(1);
            div.add(p);
            first = false;
        }
        return div;
    }

    private void addMetaSection(Document doc, Invoice invoice, Order order,
                                PdfFont bold, PdfFont normal) {
        doc.add(metaRow(bold, normal, "Date d'émission", invoice.getIssueDate().format(DATE_FORMAT)));
        if (order.getOrderedAt() != null) {
            doc.add(metaRow(bold, normal, "Date de commande",
                    order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
        doc.add(metaRow(bold, normal, "N° commande", safeStr(order.getOrderNumber())));
        if (invoice.getDueDate() != null) {
            doc.add(metaRow(bold, normal, "Date d'échéance", invoice.getDueDate().format(DATE_FORMAT)));
        }
        doc.add(new Paragraph(" ").setMarginBottom(6));
    }

    private Paragraph metaRow(PdfFont bold, PdfFont normal, String label, String value) {
        return new Paragraph()
                .add(new Text(label + " : ").setFont(bold).setFontSize(9.5f))
                .add(new Text(value).setFont(normal).setFontSize(9.5f))
                .setMarginBottom(3);
    }

    private void addItemsTable(Document doc, List<InvoiceLineItemDTO> items,
                               Order order, PdfFont bold, PdfFont normal) {
        float[] cols = {4.5f, 1f, 1f, 1.5f, 1f, 1.8f};
        Table table = new Table(UnitValue.createPercentArray(cols))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(6).setMarginBottom(6);

        // En-têtes
        String[] headers = {"DÉSIGNATION", "QTÉ", "UNIT", "P.U. HT", "TVA", "TOTAL HT"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .setBackgroundColor(COLOR_DARK)
                    .add(new Paragraph(h).setFont(bold).setFontSize(8.5f)
                            .setFontColor(ColorConstants.WHITE))
                    .setPadding(7));
        }

        // Lignes articles
        boolean even = false;
        for (InvoiceLineItemDTO item : items) {
            DeviceRgb bg = even ? COLOR_LIGHT : new DeviceRgb(255, 255, 255);
            addItemRow(table, item, bg, bold, normal);
            even = !even;
        }

        // Frais de livraison
        if (order.getShippingCost() != null && order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            DeviceRgb bg = even ? COLOR_LIGHT : new DeviceRgb(255, 255, 255);
            table.addCell(itemCell("Frais de livraison", bg, bold, false));
            table.addCell(itemCell("1", bg, normal, true));
            table.addCell(itemCell("forfait", bg, normal, true));
            table.addCell(itemCell(fmt(order.getShippingCost()) + " €", bg, normal, true));
            table.addCell(itemCell(fmtRate(invoiceConfig.getDefaultTaxRate()) + " %", bg, normal, true));
            table.addCell(itemCell(fmt(order.getShippingCost()) + " €", bg, bold, true));
        }

        doc.add(table);
    }

    private void addItemRow(Table table, InvoiceLineItemDTO item,
                            DeviceRgb bg, PdfFont bold, PdfFont normal) {
        // Désignation (nom + SKU)
        Cell nameCell = new Cell().setBackgroundColor(bg).setPadding(7).setBorderBottom(new SolidBorder(new DeviceRgb(0xDD,0xDD,0xDD), 0.5f));
        nameCell.add(new Paragraph(safeStr(item.getProductName())).setFont(bold).setFontSize(9f).setMarginBottom(1));
        if (item.getProductSku() != null && !item.getProductSku().isBlank()) {
            nameCell.add(new Paragraph("SKU : " + item.getProductSku()).setFont(normal).setFontSize(7.5f)
                    .setFontColor(new DeviceRgb(0x66, 0x66, 0x66)));
        }
        table.addCell(nameCell);

        table.addCell(itemCell(String.valueOf(item.getQuantity()), bg, normal, true));
        table.addCell(itemCell(safeStr(item.getUnit()), bg, normal, true));
        table.addCell(itemCell(fmt(item.getUnitPriceHT()) + " €", bg, normal, true));
        table.addCell(itemCell(fmtRate(item.getTaxRate()) + " %", bg, normal, true));
        table.addCell(itemCell(fmt(item.getTotalPriceHT()) + " €", bg, bold, true));
    }

    private Cell itemCell(String text, DeviceRgb bg, PdfFont font, boolean right) {
        return new Cell()
                .setBackgroundColor(bg)
                .setPadding(7)
                .setBorderBottom(new SolidBorder(new DeviceRgb(0xDD,0xDD,0xDD), 0.5f))
                .add(new Paragraph(text).setFont(font).setFontSize(9f)
                        .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
    }

    private void addTotals(Document doc, Invoice invoice, Order order,
                           PdfFont bold, PdfFont normal) {
        BigDecimal subtotal  = nvl(invoice.getSubtotal());
        BigDecimal tax       = nvl(invoice.getTaxAmount());
        BigDecimal discount  = nvl(invoice.getDiscountAmount());
        BigDecimal shipping  = nvl(order.getShippingCost());
        BigDecimal total     = nvl(invoice.getTotalAmount());

        // Tableau totaux aligné à droite (50% de largeur)
        Table t = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(12);

        addTotalRow(t, "Total HT", fmt(subtotal) + " €", false, normal, bold);
        addTotalRow(t, "TVA (" + fmtRate(invoiceConfig.getDefaultTaxRate()) + " %)", fmt(tax) + " €", false, normal, bold);

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(t, "Remise", "- " + fmt(discount) + " €", false, normal, bold, COLOR_RED);
        }
        if (shipping.compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(t, "Frais de port", fmt(shipping) + " €", false, normal, bold);
        }
        addTotalRow(t, "Net à payer TTC", fmt(total) + " €", true, bold, bold);

        doc.add(t);
    }

    private void addTotalRow(Table t, String label, String value, boolean highlight,
                             PdfFont labelFont, PdfFont valueFont) {
        addTotalRow(t, label, value, highlight, labelFont, valueFont, null);
    }

    private void addTotalRow(Table t, String label, String value, boolean highlight,
                             PdfFont labelFont, PdfFont valueFont, DeviceRgb valueColor) {
        DeviceRgb bg = highlight ? COLOR_TOTAL : new DeviceRgb(255, 255, 255);
        Border topBorder = highlight ? new SolidBorder(COLOR_DARK, 1.5f) : Border.NO_BORDER;

        Cell labelCell = new Cell().setBackgroundColor(bg).setPadding(6).setBorderTop(topBorder).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER)
                .add(new Paragraph(label).setFont(labelFont).setFontSize(highlight ? 11f : 9.5f)
                        .setFontColor(new DeviceRgb(0x55, 0x55, 0x55)));

        Paragraph valP = new Paragraph(value).setFont(valueFont).setFontSize(highlight ? 11f : 9.5f)
                .setTextAlignment(TextAlignment.RIGHT);
        if (valueColor != null) valP.setFontColor(valueColor);

        Cell valCell = new Cell().setBackgroundColor(bg).setPadding(6).setBorderTop(topBorder).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER)
                .add(valP);

        t.addCell(labelCell);
        t.addCell(valCell);
    }

    private void addPaymentInfo(Document doc, Invoice invoice, Order order,
                                PdfFont bold, PdfFont normal) {
        String statusLabel;
        DeviceRgb statusColor;
        switch (invoice.getStatus()) {
            case COMPLETED -> { statusLabel = "PAYÉ";        statusColor = COLOR_GREEN;  }
            case PENDING   -> { statusLabel = "EN ATTENTE";  statusColor = COLOR_ORANGE; }
            case FAILED    -> { statusLabel = "ÉCHOUÉ";      statusColor = COLOR_RED;    }
            case CANCELLED -> { statusLabel = "ANNULÉ";      statusColor = COLOR_RED;    }
            default        -> { statusLabel = "REMBOURSÉ";   statusColor = COLOR_ORANGE; }
        }

        Div div = new Div()
                .setBackgroundColor(new DeviceRgb(0xE8, 0xF5, 0xE9))
                .setPadding(12).setMarginTop(8).setMarginBottom(12);

        Paragraph p = new Paragraph()
                .add(new Text("Statut paiement : ").setFont(bold).setFontSize(9.5f))
                .add(new Text(statusLabel).setFont(bold).setFontSize(9.5f).setFontColor(statusColor));

        if (order.getPaymentMethod() != null) {
            p.add(new Text("    |    Mode de paiement : ").setFont(normal).setFontSize(9.5f))
                    .add(new Text(order.getPaymentMethod()).setFont(bold).setFontSize(9.5f));
        }
        if (invoice.getPaidDate() != null) {
            p.add(new Text("    |    Date de paiement : ").setFont(normal).setFontSize(9.5f))
                    .add(new Text(invoice.getPaidDate().format(DATE_FORMAT)).setFont(bold).setFontSize(9.5f));
        }

        div.add(p);
        doc.add(div);
    }

    private void addQrCode(Document doc, Invoice invoice, String qrAbsolutePath,
                           PdfFont bold, PdfFont normal) {
        String verificationUrl = buildVerificationUrl(invoice);

        Div div = new Div()
                .setBorder(new SolidBorder(new DeviceRgb(0xDD, 0xDD, 0xDD), 1))
                .setPadding(12).setMarginTop(8).setMarginBottom(12)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        div.add(new Paragraph("Vérification de la facture")
                .setFont(bold).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));

        if (qrAbsolutePath != null && !qrAbsolutePath.isBlank()) {
            try {
                Image qr = new Image(ImageDataFactory.create(qrAbsolutePath))
                        .setWidth(100).setHeight(100)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                div.add(qr);
            } catch (Exception e) {
                log.warn("Impossible d'intégrer le QR code dans le PDF: {}", e.getMessage());
            }
        }

        div.add(new Paragraph(verificationUrl)
                .setFont(normal).setFontSize(8f)
                .setFontColor(new DeviceRgb(0x66, 0x66, 0x66))
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));

        doc.add(div);
    }

    private void addFooter(Document doc, PdfFont normal) {
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setMarginTop(16).setMarginBottom(8));

        doc.add(new Paragraph("Conditions de vente : " + invoiceConfig.getLegalNotice()
                + "\n" + invoiceConfig.getLatePaymentPenalty()
                + "\n" + invoiceConfig.getCollectionFee())
                .setFont(normal).setFontSize(7.5f)
                .setFontColor(new DeviceRgb(0x77, 0x77, 0x77))
                .setMarginBottom(4));

        doc.add(new Paragraph("Page 1/1")
                .setFont(normal).setFontSize(7.5f)
                .setFontColor(new DeviceRgb(0x99, 0x99, 0x99))
                .setTextAlignment(TextAlignment.RIGHT));
    }

    // =========================================================
    // QR CODE
    // =========================================================

    private String generateQrCode(Invoice invoice) {
        try {
            Path qrDir = Paths.get(UPLOAD_DIR + "qr/");
            Files.createDirectories(qrDir);

            String url = buildVerificationUrl(invoice);
            String filename = "qr-" + invoice.getId() + ".png";
            Path qrPath = qrDir.resolve(filename);

            BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 200, 200);
            MatrixToImageWriter.writeToPath(matrix, "PNG", qrPath);

            return qrPath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("Erreur génération QR code: {}", e.getMessage());
            return "";
        }
    }

    // =========================================================
    // CONSTRUCTION LIGNES
    // =========================================================

    private List<InvoiceLineItemDTO> buildLineItems(Order order) {
        BigDecimal taxRate = invoiceConfig.getDefaultTaxRate();
        return order.getItems().stream().map(item -> {
            BigDecimal unitPrice  = nvl(item.getUnitPrice());
            BigDecimal totalPrice = nvl(item.getTotalPrice());
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;

            BigDecimal taxPerUnit = unitPrice.multiply(taxRate)
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal totalTax  = taxPerUnit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            return InvoiceLineItemDTO.builder()
                    .productName(item.getProductName() != null ? item.getProductName() : "Produit")
                    .productSku(item.getProductSku() != null ? item.getProductSku() : "")
                    .productThumbnail(item.getProductThumbnail())
                    .quantity(qty).unit("pc")
                    .unitPriceHT(unitPrice)
                    .taxRate(taxRate)
                    .taxAmount(totalTax)
                    .totalPriceHT(totalPrice)
                    .totalPriceTTC(totalPrice.add(totalTax))
                    .build();
        }).collect(Collectors.toList());
    }

    // =========================================================
    // MÉTHODES DE LECTURE
    // =========================================================

    public InvoiceDTO getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable: " + id));
        return toDTO(invoice);
    }

    public List<InvoiceDTO> getInvoicesByOrder(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<InvoiceDTO> getInvoicesByUser(Long userId) {
        return invoiceRepository.findByUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public File getPdfFile(String pdfUrl) {
        Path path = Paths.get("." + pdfUrl);
        if (!Files.exists(path)) throw new RuntimeException("Fichier PDF introuvable: " + pdfUrl);
        return path.toFile();
    }

    @Transactional
    public InvoiceDTO regeneratePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture introuvable: " + invoiceId));
        String qrPath = generateQrCode(invoice);
        String pdfPath = generatePdf(invoice, invoice.getOrder(), qrPath);
        invoice.setPdfUrl(pdfPath);
        invoiceRepository.save(invoice);
        return toDTO(invoice);
    }

    // =========================================================
    // CONVERSION ENTITY → DTO
    // =========================================================

    public InvoiceDTO toDTO(Invoice invoice) {
        Order order = invoice.getOrder();
        String customerName  = "";
        String customerEmail = "";
        String customerPhone = "";

        if (order != null) {
            customerName  = safe(order.getShippingName(), order.getUser() != null ? order.getUser().getName() : "");
            customerPhone = safe(order.getShippingPhone(), "");
            if (order.getUser() != null) {
                customerEmail = order.getUser().getEmail();
                if (customerPhone.isBlank()) customerPhone = safeStr(order.getUser().getPhone());
            }
        }

        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidDate() != null ? invoice.getPaidDate().atStartOfDay() : null)
                .company(invoiceConfig.getCompanyInfo())
                .customerId(invoice.getUser() != null ? invoice.getUser().getId() : null)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .billingAddress(invoice.getBillingAddress())
                .items(order != null ? buildLineItems(order) : List.of())
                .subtotalHT(invoice.getSubtotal())
                .totalDiscount(invoice.getDiscountAmount())
                .totalTax(invoice.getTaxAmount())
                .totalShipping(order != null ? order.getShippingCost() : BigDecimal.ZERO)
                .totalTTC(invoice.getTotalAmount())
                .paymentStatus(invoice.getStatus())
                .paymentMethod(order != null ? order.getPaymentMethod() : null)
                .pdfUrl(invoice.getPdfUrl())
                .qrCodeUrl(invoice.getPdfUrl() != null
                        ? invoice.getPdfUrl().replace("/pdf/", "/qr/").replace(".pdf", ".png") : null)
                .verificationUrl(buildVerificationUrl(invoice))
                .notes(invoice.getNotes())
                .legalNotice(invoiceConfig.getLegalNotice())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    private String buildBillingAddress(Order order) {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, order.getShippingName());
        appendLine(sb, order.getShippingAddress());
        if (order.getShippingPostalCode() != null || order.getShippingCity() != null) {
            sb.append(safeStr(order.getShippingPostalCode())).append(" ")
                    .append(safeStr(order.getShippingCity())).append("\n");
        }
        appendLine(sb, order.getShippingCountry());
        return sb.toString().trim();
    }

    private String buildVerificationUrl(Invoice invoice) {
        return invoiceConfig.getCompanyWebsite() + "/verify/" + invoice.getInvoiceNumber();
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "0,00";
        return String.format("%,.2f", v).replace(",", " ").replace(".", ",");
    }

    private String fmtRate(BigDecimal v) {
        return v != null ? v.stripTrailingZeros().toPlainString() : "0";
    }

    private BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private String safeStr(String s)     { return s != null ? s : ""; }
    private String safe(String a, String b) { return (a != null && !a.isBlank()) ? a : safeStr(b); }

    private void appendLine(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) sb.append(value).append("\n");
    }
}