package com.lcwd.electronicStore.ElectronicStore.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RazorpayPaymentVerificationRequest {

    @NotBlank(message = "Local order id is required !!")
    private String orderId;

    @NotBlank(message = "Razorpay order id is required !!")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment id is required !!")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required !!")
    private String razorpaySignature;
}
