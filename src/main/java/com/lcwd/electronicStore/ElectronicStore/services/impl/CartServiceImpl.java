package com.lcwd.electronicStore.ElectronicStore.services.impl;

/*
Purpose:
Implements cart behavior including item add, quantity update, remove, clear, and totals.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.AddItemToCartRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.CartDto;
import com.lcwd.electronicStore.ElectronicStore.entities.Cart;
import com.lcwd.electronicStore.ElectronicStore.entities.CartItem;
import com.lcwd.electronicStore.ElectronicStore.entities.Product;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.exceptions.BadApiRequestException;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.repositories.CartItemRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.CartRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.ProductRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.services.CartService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;


@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartDto addItemToCart(String userId, AddItemToCartRequest request) {

        // ===================== 1. VALIDATION =====================
        int quantity = request.getQuantity();
        String productId = request.getProductId();

        if (quantity <= 0) {
            throw new BadApiRequestException("Requested quantity is not valid !!");
        }

        // ===================== 2. FETCH PRODUCT =====================
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in database !!"));

        //  STOCK VALIDATION
        if (quantity > product.getQuantity()) {
            throw new BadApiRequestException("No more items available in stock !!");
        }

        // ===================== 3. FETCH USER =====================
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in database !!"));

        // ===================== 4. FETCH OR CREATE CART =====================
        Cart cart;

        try {
            cart = cartRepository.findByUser(user).get();
        } catch (NoSuchElementException e) {
            cart = new Cart();
            cart.setCartId(UUID.randomUUID().toString());
            cart.setCreatedAt(LocalDateTime.now());
            cart.setItems(new ArrayList<>());
        }

        // ===================== 5. UPDATE EXISTING ITEM =====================
        boolean updated = false;

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getProductId().equals(productId)) {

                //  ADD QUANTITY (IMPORTANT FIX)
                int newQuantity = item.getQuantity() + quantity;

                //  CHECK AGAINST STOCK
                if (newQuantity > product.getQuantity()) {
                    throw new BadApiRequestException(
                            "No more items available in stock !!"
                    );
                }

                item.setQuantity(newQuantity);
                item.setTotalPrice(newQuantity * product.getDiscountedPrice());

                updated = true;
                break;
            }
        }

        // ===================== 6. ADD NEW ITEM =====================
        if (!updated) {
            CartItem cartItem = CartItem.builder()
                    .quantity(quantity)
                    .totalPrice(quantity * product.getDiscountedPrice())
                    .cart(cart)
                    .product(product)
                    .build();

            cart.getItems().add(cartItem);
        }

        // ===================== 7. SET RELATION =====================
        cart.setUser(user);

        // ===================== 8. SAVE =====================
        Cart savedCart = cartRepository.save(cart);

        return mapper.map(savedCart, CartDto.class);
    }
    @Override
    public void removeItemFromCart(String userId, int cartItem) {
        //conditions

        CartItem cartItem1 = cartItemRepository.findById(cartItem).orElseThrow(() -> new ResourceNotFoundException("Cart Item not found !!"));
        Cart cart = cartItem1.getCart();
        if (!cart.getUser().getUserId().equals(userId)) {
            throw new BadApiRequestException("Cart item does not belong to this user !!");
        }
        cart.getItems().remove(cartItem1);
        cartItemRepository.delete(cartItem1);
        cartRepository.save(cart);

    }

    @Override
    public CartDto updateItemQuantity(String userId, int cartItem, int quantity) {
        if (quantity < 0) {
            throw new BadApiRequestException("Requested quantity is not valid !!");
        }

        CartItem cartItemEntity = cartItemRepository.findById(cartItem)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found !!"));

        Cart cart = cartItemEntity.getCart();
        if (!cart.getUser().getUserId().equals(userId)) {
            throw new BadApiRequestException("Cart item does not belong to this user !!");
        }

        if (quantity == 0) {
            cart.getItems().remove(cartItemEntity);
            cartItemRepository.delete(cartItemEntity);
            return mapper.map(cartRepository.save(cart), CartDto.class);
        }

        Product product = cartItemEntity.getProduct();
        if (quantity > product.getQuantity()) {
            throw new BadApiRequestException("No more items available in stock !!");
        }

        cartItemEntity.setQuantity(quantity);
        cartItemEntity.setTotalPrice(quantity * product.getDiscountedPrice());
        cartItemRepository.save(cartItemEntity);

        return mapper.map(cartRepository.save(cart), CartDto.class);
    }

    @Override
    public void clearCart(String userId) {
        //fetch the user from db
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user not found in database!!"));
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Cart of given user not found !!"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public CartDto getCartByUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user not found in database!!"));
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setCartId(UUID.randomUUID().toString());
            newCart.setCreatedAt(LocalDateTime.now());
            newCart.setUser(user);
            newCart.setItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });
        return mapper.map(cart, CartDto.class);
    }
}

