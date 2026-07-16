package com.lcwd.electronicStore.ElectronicStore.controller;
import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayOrderResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentFailureRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentVerificationRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentVerificationResponse;
import com.lcwd.electronicStore.ElectronicStore.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //create
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/razorpay")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(@Valid @RequestBody CreateOrderRequest request) {
        RazorpayOrderResponse response = orderService.createRazorpayOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<RazorpayPaymentVerificationResponse> verifyRazorpayPayment(
            @Valid @RequestBody RazorpayPaymentVerificationRequest request
    ) {
        return ResponseEntity.ok(orderService.verifyRazorpayPayment(request));
    }

    @PostMapping("/razorpay/failure")
    public ResponseEntity<OrderDto> recordRazorpayPaymentFailure(
            @Valid @RequestBody RazorpayPaymentFailureRequest request
    ) {
        return ResponseEntity.ok(orderService.recordRazorpayPaymentFailure(request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse> removeOrder(@PathVariable String orderId) {
        orderService.removeOrder(orderId);
        ApiResponse responseMessage = ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("order is removed !!")
                .successs(true)
                .build();
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);

    }
    //UPDATE ORDER STATUS
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable String orderId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    //get orders of the user

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersOfUser(@PathVariable String userId) {
        List<OrderDto> ordersOfUser = orderService.getOrdersOfUser(userId);
        return new ResponseEntity<>(ordersOfUser, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageableResponse<OrderDto>> getOrders(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "orderedDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir
    ) {
        PageableResponse<OrderDto> orders = orderService.getOrders(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


}

