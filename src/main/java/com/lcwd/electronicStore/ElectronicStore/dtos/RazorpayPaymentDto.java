package com.lcwd.electronicStore.ElectronicStore.dtos;

/*
Purpose:
Groups Razorpay order, verification, and failure request/response DTOs in one place.
*/
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class RazorpayPaymentDto {

    private RazorpayPaymentDto() {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderResponse {

        private String keyId;
        private String razorpayOrderId;
        private long amount;
        private String currency;
        private OrderDto order;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class VerificationRequest {

        @NotBlank(message = "Local order id is required !!")
        private String orderId;

        @NotBlank(message = "Razorpay order id is required !!")
        private String razorpayOrderId;

        @NotBlank(message = "Razorpay payment id is required !!")
        private String razorpayPaymentId;

        @NotBlank(message = "Razorpay signature is required !!")
        private String razorpaySignature;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class VerificationResponse {

        private boolean success;
        private String message;
        private OrderDto order;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FailureRequest {

        @NotBlank(message = "Local order id is required !!")
        private String orderId;

        @NotBlank(message = "Razorpay order id is required !!")
        private String razorpayOrderId;

        private String razorpayPaymentId;
        private String code;
        private String reason;
        private String description;
    }
}
