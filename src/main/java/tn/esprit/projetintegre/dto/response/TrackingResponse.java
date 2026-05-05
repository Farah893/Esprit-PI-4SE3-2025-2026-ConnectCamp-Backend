package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {

    private Long orderId;
    private String orderNumber;
    private String trackingNumber;
    private String carrierName;
    private String carrierWebsite;
    private String carrierTrackingUrl;

    private CurrentTrackingStatus currentStatus;
    private EstimatedDelivery estimatedDelivery;
    private List<TrackingEvent> events;

    private ShippingInfo shippingInfo;
    private Instant lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfo {
        private String recipientName;
        private String address;
        private String city;
        private String postalCode;
        private String country;
    }
}
