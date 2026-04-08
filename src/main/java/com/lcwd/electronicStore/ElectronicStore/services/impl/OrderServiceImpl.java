package com.lcwd.electronicStore.ElectronicStore.services.impl;
import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.entities.*;
import com.lcwd.electronicStore.ElectronicStore.exceptions.BadApiRequestException;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.helper.PageableHelper;
import com.lcwd.electronicStore.ElectronicStore.repositories.CartRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.OrderRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.services.OrderService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PageableHelper pageableHelper;

    // 🔥 CREATE ORDER
    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {

        // 1. Fetch User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));

        // 2. Fetch Cart
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found !!"));

        // 3. Validate Cart
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadApiRequestException("Cart is empty !!");
        }

        // 4. Create Order
        Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .billingName(request.getBillingName())
                .billingPhone(request.getBillingPhone())
                .billingAddress(request.getBillingAddress())
                .orderStatus(request.getOrderStatus())
                .paymentStatus(request.getPaymentStatus())
                .orderedDate(LocalDateTime.now())
                .expectedDeliveryDate(LocalDateTime.now().plusDays(5))
                .user(user)
                .build();

        // 5. Convert CartItem -> OrderItem
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {

            OrderItem item = OrderItem.builder()
                    .quantity(cartItem.getQuantity())
                    .product(cartItem.getProduct())
                    .totalPrice(cartItem.getQuantity() * cartItem.getProduct().getDiscountedPrice())
                    .order(order) //  VERY IMPORTANT
                    .build();

            return item;

        }).collect(Collectors.toList());

        // 6. Set OrderItems
        order.setOrderItems(orderItems);

        // 7. Calculate Total Amount
        int totalAmount = orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();

        order.setOrderAmount(totalAmount);

        // 8. Clear Cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // 9. Save Order (Cascade saves OrderItems)
        Order savedOrder = orderRepository.save(order);

        // 10. Convert to DTO
        return modelMapper.map(savedOrder, OrderDto.class);
    }

    // DELETE ORDER
    @Override
    @Transactional
    public void removeOrder(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found !!"));

        orderRepository.delete(order);
    }
    //UPDATE ORDER STATUS
    @Override
    public OrderDto updateOrderStatus(String orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        //VALIDATION
        List<String> validStatus = List.of("PENDING", "DISPATCHED", "DELIVERED");
        if (!validStatus.contains(status.toUpperCase())) {
            throw new RuntimeException("Invalid status");
        }

        order.setOrderStatus(status.toUpperCase());

        Order updated = orderRepository.save(order);

        return modelMapper.map(updated, OrderDto.class);
    }

    // GET ORDERS OF USER
    @Override
    public List<OrderDto> getOrdersOfUser(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));

        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    // GET ALL ORDERS (PAGINATION)
    @Override
    public PageableResponse<OrderDto> getOrders(int pageNumber, int pageSize, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Order> page = orderRepository.findAll(pageable);

        return pageableHelper.getPageableResponse(page, OrderDto.class);
    }
}