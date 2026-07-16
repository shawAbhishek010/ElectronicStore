package com.lcwd.electronicStore.ElectronicStore.services;


import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayOrderResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentFailureRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentVerificationRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentVerificationResponse;

import java.util.List;

public interface OrderService {

    //create order
    OrderDto createOrder(CreateOrderRequest orderDto);

    RazorpayOrderResponse createRazorpayOrder(CreateOrderRequest request);

    RazorpayPaymentVerificationResponse verifyRazorpayPayment(RazorpayPaymentVerificationRequest request);

    OrderDto recordRazorpayPaymentFailure(RazorpayPaymentFailureRequest request);

    //remove order
    void removeOrder(String orderId);

    //get orders of user
    List<OrderDto> getOrdersOfUser(String userId);
    //UPDATE ORDER STATUS
    OrderDto updateOrderStatus(String orderId, String status);

    //get orders
    PageableResponse<OrderDto> getOrders(int pageNumber, int pageSize, String sortBy, String sortDir);


}
