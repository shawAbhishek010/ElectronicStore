package com.lcwd.electronicStore.ElectronicStore.controller;
/*
Purpose:
Exposes order, Razorpay payment, admin status update, and user delivery confirmation APIs.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.FailureRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.OrderResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.VerificationRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.VerificationResponse;
import com.lcwd.electronicStore.ElectronicStore.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //create
    @PostMapping
    @PreAuthorize("hasRole('USER') and @securityGuard.isCurrentUserId(#request.userId)")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/razorpay")
    @PreAuthorize("hasRole('USER') and @securityGuard.isCurrentUserId(#request.userId)")
    public ResponseEntity<OrderResponse> createRazorpayOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createRazorpayOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/razorpay/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VerificationResponse> verifyRazorpayPayment(
            @Valid @RequestBody VerificationRequest request
    ) {
        return ResponseEntity.ok(orderService.verifyRazorpayPayment(request));
    }

    @PostMapping("/razorpay/failure")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> recordRazorpayPaymentFailure(
            @Valid @RequestBody FailureRequest request
    ) {
        return ResponseEntity.ok(orderService.recordRazorpayPaymentFailure(request));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable String orderId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PutMapping("/{orderId}/confirm-delivery")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> confirmDelivery(
            @PathVariable String orderId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(orderService.confirmDelivery(orderId, authentication.getName()));
    }

    //get orders of the user

    @GetMapping("/users/{userId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<List<OrderDto>> getOrdersOfUser(@PathVariable String userId) {
        List<OrderDto> ordersOfUser = orderService.getOrdersOfUser(userId);
        return new ResponseEntity<>(ordersOfUser, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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

