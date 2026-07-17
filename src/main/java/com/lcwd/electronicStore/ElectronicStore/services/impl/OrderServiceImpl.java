package com.lcwd.electronicStore.ElectronicStore.services.impl;
/*
Purpose:
Implements order creation, Razorpay payment handling, status transitions, and order queries.
*/
import com.lcwd.electronicStore.ElectronicStore.config.RazorpayConfig;
import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.FailureRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.OrderResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.VerificationRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.VerificationResponse;
import com.lcwd.electronicStore.ElectronicStore.entities.*;
import com.lcwd.electronicStore.ElectronicStore.exceptions.BadApiRequestException;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.helper.PageableHelper;
import com.lcwd.electronicStore.ElectronicStore.helper.PaymentAmountHelper;
import com.lcwd.electronicStore.ElectronicStore.repositories.CartRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.OrderRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.services.OrderService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final List<String> RETRYABLE_PAYMENT_STATUSES = List.of(
            "PAYMENT_PENDING",
            "PAYMENT_FAILED"
    );
    private static final String ORDER_PENDING = "PENDING";
    private static final String ORDER_PAID = "PAID";
    private static final String ORDER_SHIPPED = "SHIPPED";
    private static final String ORDER_DELIVERED = "DELIVERED";
    private static final String ORDER_COMPLETED = "COMPLETED";
    private static final String PAYMENT_PAID = "PAID";
    private static final String PAYMENT_PENDING = "PAYMENT_PENDING";

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

    @Autowired
    private RazorpayConfig razorpayConfig;

    // 🔥 CREATE ORDER
    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        request.setOrderStatus(ORDER_PENDING);
        request.setPaymentStatus("NOTPAID");
        return createOrder(request, true);
    }

    private OrderDto createOrder(CreateOrderRequest request, boolean clearCart) {

        // 1. Fetch User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));

        // 2. Fetch Cart
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found !!"));

        if (cart.getUser() == null || !cart.getUser().getUserId().equals(user.getUserId())) {
            throw new BadApiRequestException("Cart does not belong to this user !!");
        }
        ensureCurrentUserOwns(user);

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
        long totalAmount = orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();

        order.setOrderAmount(totalAmount);

        // Razorpay orders keep the cart intact until payment is verified so a
        // declined or dismissed checkout can be retried.
        if (clearCart) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }

        // 9. Save Order (Cascade saves OrderItems)
        Order savedOrder = orderRepository.save(order);

        // 10. Convert to DTO
        return modelMapper.map(savedOrder, OrderDto.class);
    }

    @Override
    @Transactional
    public OrderResponse createRazorpayOrder(CreateOrderRequest request) {
        validateRazorpayConfig();

        request.setOrderStatus(ORDER_PENDING);
        request.setPaymentStatus(PAYMENT_PENDING);

        Optional<Order> reusableOrder = findReusableRazorpayOrder(request);
        if (reusableOrder.isPresent()) {
            Order order = reusableOrder.get();
            order.setBillingName(request.getBillingName());
            order.setBillingPhone(request.getBillingPhone());
            order.setBillingAddress(request.getBillingAddress());
            order.setPaymentStatus(PAYMENT_PENDING);
            return buildRazorpayOrderResponse(orderRepository.save(order));
        }

        OrderDto order = createOrder(request, false);
        long amountInPaise = PaymentAmountHelper.toPaise(order.getOrderAmount());

        Map<String, Object> razorpayOrder = razorpayClient().post()
                .uri("/orders")
                .body(Map.of(
                        "amount", amountInPaise,
                        "currency", razorpayConfig.getCurrency(),
                        "receipt", order.getOrderId(),
                        "notes", Map.of("localOrderId", order.getOrderId())
                ))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (requestSpec, responseSpec) -> {
                    throw new BadApiRequestException("Unable to create Razorpay order. Please check payment configuration.");
                })
                .body(Map.class);

        String razorpayOrderId = razorpayOrder == null ? null : String.valueOf(razorpayOrder.get("id"));
        if (!StringUtils.hasText(razorpayOrderId)) {
            throw new BadApiRequestException("Razorpay did not return an order id.");
        }

        Order savedOrder = orderRepository.findById(order.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found !!"));
        savedOrder.setRazorpayOrderId(razorpayOrderId);
        savedOrder = orderRepository.save(savedOrder);

        return buildRazorpayOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public VerificationResponse verifyRazorpayPayment(VerificationRequest request) {
        validateRazorpayConfig();

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found !!"));
        ensureCurrentUserOwns(order.getUser());

        if (!request.getRazorpayOrderId().equals(order.getRazorpayOrderId())) {
            throw new BadApiRequestException("Razorpay order id does not match this order.");
        }

        if (!isValidSignature(request)) {
            order.setPaymentStatus("PAYMENT_VERIFICATION_FAILED");
            orderRepository.save(order);
            throw new BadApiRequestException("Payment signature verification failed.");
        }

        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setRazorpaySignature(request.getRazorpaySignature());
        order.setPaymentStatus(PAYMENT_PAID);
        order.setOrderStatus(ORDER_PAID);
        order.setRazorpayFailedPaymentId(null);
        order.setPaymentFailureCode(null);
        order.setPaymentFailureReason(null);
        order.setPaymentFailureDescription(null);
        Order savedOrder = orderRepository.save(order);

        cartRepository.findByUser(order.getUser()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });

        return VerificationResponse.builder()
                .success(true)
                .message("Payment verified successfully.")
                .order(modelMapper.map(savedOrder, OrderDto.class))
                .build();
    }

    @Override
    @Transactional
    public OrderDto recordRazorpayPaymentFailure(FailureRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found !!"));
        ensureCurrentUserOwns(order.getUser());

        if (!request.getRazorpayOrderId().equals(order.getRazorpayOrderId())) {
            throw new BadApiRequestException("Razorpay order id does not match this order.");
        }

        if (PAYMENT_PAID.equals(order.getPaymentStatus())) {
            return modelMapper.map(order, OrderDto.class);
        }

        order.setPaymentStatus("PAYMENT_FAILED");
        order.setRazorpayFailedPaymentId(request.getRazorpayPaymentId());
        order.setPaymentFailureCode(request.getCode());
        order.setPaymentFailureReason(request.getReason());
        order.setPaymentFailureDescription(request.getDescription());

        return modelMapper.map(orderRepository.save(order), OrderDto.class);
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

        String requestedStatus = normalizeOrderStatus(status);
        if (!ORDER_SHIPPED.equals(requestedStatus) && !ORDER_DELIVERED.equals(requestedStatus)) {
            throw new BadApiRequestException("Admin can only mark paid orders as SHIPPED or DELIVERED.");
        }

        if (ORDER_SHIPPED.equals(requestedStatus) && !ORDER_PAID.equals(order.getOrderStatus())) {
            throw new BadApiRequestException("Only PAID orders can be marked as SHIPPED.");
        }

        if (ORDER_DELIVERED.equals(requestedStatus) && !isShippedStatus(order.getOrderStatus())) {
            throw new BadApiRequestException("Only SHIPPED orders can be marked as DELIVERED.");
        }

        order.setOrderStatus(requestedStatus);

        Order updated = orderRepository.save(order);

        return modelMapper.map(updated, OrderDto.class);
    }

    @Override
    @Transactional
    public OrderDto confirmDelivery(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getUser() == null || !userEmail.equals(order.getUser().getEmail())) {
            throw new BadApiRequestException("You can confirm only your own delivery.");
        }

        if (!ORDER_DELIVERED.equals(order.getOrderStatus())) {
            throw new BadApiRequestException("Only DELIVERED orders can be confirmed.");
        }

        order.setOrderStatus(ORDER_COMPLETED);
        return modelMapper.map(orderRepository.save(order), OrderDto.class);
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

    private Optional<Order> findReusableRazorpayOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found !!"));

        if (cart.getUser() == null || !cart.getUser().getUserId().equals(user.getUserId())) {
            throw new BadApiRequestException("Cart does not belong to this user !!");
        }
        ensureCurrentUserOwns(user);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadApiRequestException("Cart is empty !!");
        }

        return orderRepository
                .findFirstByUserAndPaymentStatusInOrderByOrderedDateDesc(user, RETRYABLE_PAYMENT_STATUSES)
                .filter(order -> StringUtils.hasText(order.getRazorpayOrderId()))
                .filter(order -> hasSameCartSnapshot(order, cart));
    }

    private boolean hasSameCartSnapshot(Order order, Cart cart) {
        if (order.getOrderItems().size() != cart.getItems().size()) {
            return false;
        }

        Map<String, ItemSnapshot> orderItems = order.getOrderItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        item -> new ItemSnapshot(item.getQuantity(), item.getTotalPrice())
                ));
        Map<String, ItemSnapshot> cartItems = cart.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        item -> new ItemSnapshot(item.getQuantity(), item.getTotalPrice())
                ));

        return orderItems.equals(cartItems);
    }

    private OrderResponse buildRazorpayOrderResponse(Order order) {
        return OrderResponse.builder()
                .keyId(razorpayConfig.getKeyId())
                .razorpayOrderId(order.getRazorpayOrderId())
                .amount(PaymentAmountHelper.toPaise(order.getOrderAmount()))
                .currency(razorpayConfig.getCurrency())
                .order(modelMapper.map(order, OrderDto.class))
                .build();
    }

    private RestClient razorpayClient() {
        return RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(headers -> headers.setBasicAuth(razorpayConfig.getKeyId(), razorpayConfig.getKeySecret()))
                .build();
    }

    private void validateRazorpayConfig() {
        if (!StringUtils.hasText(razorpayConfig.getKeyId()) || !StringUtils.hasText(razorpayConfig.getKeySecret())) {
            throw new BadApiRequestException("Razorpay key id or secret is missing. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }
    }

    private String normalizeOrderStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BadApiRequestException("Order status is required.");
        }
        String requestedStatus = status.trim().toUpperCase();
        if ("DISPATCHED".equals(requestedStatus)) {
            return ORDER_SHIPPED;
        }
        return requestedStatus;
    }

    private boolean isShippedStatus(String status) {
        return ORDER_SHIPPED.equals(status) || "DISPATCHED".equals(status);
    }

    private void ensureCurrentUserOwns(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || user == null) {
            throw new BadApiRequestException("Unable to verify the current user.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin && !authentication.getName().equals(user.getEmail())) {
            throw new BadApiRequestException("You can access only your own orders.");
        }
    }

    private boolean isValidSignature(VerificationRequest request) {
        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayConfig.getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expectedSignature = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    request.getRazorpaySignature().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            throw new BadApiRequestException("Unable to verify payment signature.");
        }
    }

    private record ItemSnapshot(int quantity, long totalPrice) {
    }
}
