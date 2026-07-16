package com.lcwd.electronicStore.ElectronicStore.dtos;

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
public class RazorpayPaymentVerificationResponse {

    private boolean success;
    private String message;
    private OrderDto order;
}
