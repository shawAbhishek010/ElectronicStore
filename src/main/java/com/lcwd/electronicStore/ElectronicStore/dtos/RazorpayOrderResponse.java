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
public class RazorpayOrderResponse {

    private String keyId;
    private String razorpayOrderId;
    private long amount;
    private String currency;
    private OrderDto order;
}
