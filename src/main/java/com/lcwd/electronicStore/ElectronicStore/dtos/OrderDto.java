package com.lcwd.electronicStore.ElectronicStore.dtos;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderDto {

    private String orderId;
    private String orderStatus="PENDING";
    private String paymentStatus="NOT PAID";
    private int orderAmount;
    private String billingAddress;
    private String billingPhone;
    private String billingName;
    private LocalDateTime orderedDate;
    private LocalDateTime expectedDeliveryDate;
    private List<OrderItemDto> orderItems = new ArrayList<>();


}