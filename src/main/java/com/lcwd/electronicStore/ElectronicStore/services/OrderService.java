package com.lcwd.electronicStore.ElectronicStore.services;


/*
Purpose:
Defines order, payment, status transition, and order lookup business operations.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.FailureRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.OrderResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.VerificationRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.VerificationResponse;

import java.util.List;

public interface OrderService {

    //create order
    OrderDto createOrder(CreateOrderRequest orderDto);

    OrderResponse createRazorpayOrder(CreateOrderRequest request);

    VerificationResponse verifyRazorpayPayment(VerificationRequest request);

    OrderDto recordRazorpayPaymentFailure(FailureRequest request);

    //remove order
    void removeOrder(String orderId);

    //get orders of user
    List<OrderDto> getOrdersOfUser(String userId);
    //UPDATE ORDER STATUS
    OrderDto updateOrderStatus(String orderId, String status);

    OrderDto confirmDelivery(String orderId, String userEmail);

    //get orders
    PageableResponse<OrderDto> getOrders(int pageNumber, int pageSize, String sortBy, String sortDir);


}
