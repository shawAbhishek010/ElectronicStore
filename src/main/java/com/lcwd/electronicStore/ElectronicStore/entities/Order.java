package com.lcwd.electronicStore.ElectronicStore.entities;

/*
Purpose:
Represents a customer order, payment metadata, delivery status, and order items.
*/
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String orderId;

    //PENDING, PAID, SHIPPED, DELIVERED, COMPLETED
    private String orderStatus;

    private String paymentStatus;

    @Column(columnDefinition = "BIGINT")
    private long orderAmount;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    @Column(length = 1000)
    private String razorpaySignature;

    private String razorpayFailedPaymentId;

    private String paymentFailureCode;

    private String paymentFailureReason;

    @Column(length = 1000)
    private String paymentFailureDescription;

    @Column(length = 1000)
    private String billingAddress;

    private String billingPhone;

    private String billingName;

    private LocalDateTime orderedDate;

    private LocalDateTime expectedDeliveryDate;

    //user
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();


}
