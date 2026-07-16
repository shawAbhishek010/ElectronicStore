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
    private long orderAmount;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String razorpayFailedPaymentId;
    private String paymentFailureCode;
    private String paymentFailureReason;
    private String paymentFailureDescription;
    private String billingAddress;
    private String billingPhone;
    private String billingName;
    private LocalDateTime orderedDate;
    private LocalDateTime expectedDeliveryDate;
    private List<OrderItemDto> orderItems = new ArrayList<>();


}
