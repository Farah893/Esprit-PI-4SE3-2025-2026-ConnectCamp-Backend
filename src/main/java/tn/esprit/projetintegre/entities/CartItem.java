package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "pack_id")
    private Pack pack;

    private Integer quantity = 1;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    private String selectedVariant;
    private String selectedColor;
    private String selectedSize;

    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        if (price == null) {
            if (product != null) {
                price = product.getPrice();
            } else if (pack != null) {
                price = pack.getPrice();
            }
        }
    }
}
