package com.lcwd.electronicStore.ElectronicStore.services.impl;

import com.lcwd.electronicStore.ElectronicStore.config.RazorpayConfig;
import com.lcwd.electronicStore.ElectronicStore.dtos.CreateOrderRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.OrderDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.RazorpayPaymentDto.OrderResponse;
import com.lcwd.electronicStore.ElectronicStore.entities.Cart;
import com.lcwd.electronicStore.ElectronicStore.entities.CartItem;
import com.lcwd.electronicStore.ElectronicStore.entities.Order;
import com.lcwd.electronicStore.ElectronicStore.entities.OrderItem;
import com.lcwd.electronicStore.ElectronicStore.entities.Product;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.repositories.CartRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.OrderRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void configureRazorpay() {
        RazorpayConfig config = new RazorpayConfig();
        config.setKeyId("rzp_test_example");
        config.setKeySecret("test_secret");
        config.setCurrency("INR");
        ReflectionTestUtils.setField(orderService, "razorpayConfig", config);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reusesLatestPendingRazorpayOrderWhenCartSnapshotMatches() {
        User user = new User();
        user.setUserId("user-1");
        user.setEmail("customer@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "customer@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        Product product = new Product();
        product.setProductId("product-1");
        product.setDiscountedPrice(200_000L);

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(1)
                .totalPrice(200_000L)
                .build();
        Cart cart = Cart.builder()
                .cartId("cart-1")
                .user(user)
                .items(List.of(cartItem))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(1)
                .totalPrice(200_000L)
                .build();
        Order pendingOrder = Order.builder()
                .orderId("local-order-1")
                .orderAmount(200_000L)
                .paymentStatus("PAYMENT_PENDING")
                .razorpayOrderId("order_existing")
                .orderedDate(LocalDateTime.now())
                .user(user)
                .orderItems(List.of(orderItem))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .cartId("cart-1")
                .userId("user-1")
                .billingName("Updated Name")
                .billingPhone("9999999999")
                .billingAddress("Updated Address")
                .build();
        OrderDto orderDto = OrderDto.builder()
                .orderId("local-order-1")
                .orderAmount(200_000L)
                .razorpayOrderId("order_existing")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(cartRepository.findById("cart-1")).thenReturn(Optional.of(cart));
        when(orderRepository.findFirstByUserAndPaymentStatusInOrderByOrderedDateDesc(eq(user), anyList()))
                .thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(pendingOrder)).thenReturn(pendingOrder);
        when(modelMapper.map(pendingOrder, OrderDto.class)).thenReturn(orderDto);

        OrderResponse response = orderService.createRazorpayOrder(request);

        assertEquals("order_existing", response.getRazorpayOrderId());
        assertEquals(20_000_000L, response.getAmount());
        assertEquals("Updated Address", pendingOrder.getBillingAddress());
        verify(orderRepository).save(pendingOrder);
        verify(cartRepository, never()).save(cart);
    }
}
