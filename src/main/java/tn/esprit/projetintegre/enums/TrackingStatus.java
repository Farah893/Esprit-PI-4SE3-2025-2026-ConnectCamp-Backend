package tn.esprit.projetintegre.enums;

public enum TrackingStatus {

    LABEL_CREATED("Label created", "info"),
    PICKED_UP("Picked up by carrier", "info"),
    IN_TRANSIT("In transit", "warning"),
    OUT_FOR_DELIVERY("Out for delivery", "warning"),
    DELIVERED("Delivered", "success"),
    AT_PICKUP_POINT("Available at pickup point", "info"),
    DELIVERY_ATTEMPTED("Delivery attempt failed", "error"),
    EXCEPTION("Delivery exception", "error"),
    RETURNED_TO_SENDER("Returned to sender", "error"),
    NOT_SHIPPED("Order being prepared", "info");

    private final String label;
    private final String severity;

    TrackingStatus(String label, String severity) {
        this.label = label;
        this.severity = severity;
    }

    public String getLabel() {
        return label;
    }

    public String getSeverity() {
        return severity;
    }
}