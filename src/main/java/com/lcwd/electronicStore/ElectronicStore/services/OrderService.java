package com.lcwd.electronicStore.ElectronicStore.services;


import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;

import java.util.List;

public interface OrderService {

    //create order
    OrderDto createOrder(CreateOrderRequest orderDto);

    //remove order
    void removeOrder(String orderId);

    //get orders of user
    List<OrderDto> getOrdersOfUser(String userId);
    //UPDATE ORDER STATUS
    OrderDto updateOrderStatus(String orderId, String status);

    //get orders
    PageableResponse<OrderDto> getOrders(int pageNumber, int pageSize, String sortBy, String sortDir);

    //order methods(logic) related to order

}
