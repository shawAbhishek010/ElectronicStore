package com.lcwd.electronicStore.ElectronicStore.controller;

/*
Purpose:
Exposes wishlist APIs for saving, listing, and removing products for a user.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.ProductDto;
import com.lcwd.electronicStore.ElectronicStore.entities.Product;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.entities.WishlistItem;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.repositories.ProductRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.WishlistItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public WishlistController(WishlistItemRepository wishlistItemRepository,
                              UserRepository userRepository,
                              ProductRepository productRepository,
                              ModelMapper modelMapper) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<List<ProductDto>> getWishlist(@PathVariable String userId) {
        User user = getUser(userId);
        return ResponseEntity.ok(toProductDtos(wishlistItemRepository.findByUserOrderByCreatedAtDesc(user)));
    }

    @PostMapping("/{userId}/products/{productId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<List<ProductDto>> addToWishlist(@PathVariable String userId, @PathVariable String productId) {
        User user = getUser(userId);
        Product product = getProduct(productId);

        if (!wishlistItemRepository.existsByUserUserIdAndProductProductId(userId, productId)) {
            WishlistItem item = WishlistItem.builder()
                    .user(user)
                    .product(product)
                    .createdAt(LocalDateTime.now())
                    .build();
            wishlistItemRepository.save(item);
        }

        return ResponseEntity.ok(toProductDtos(wishlistItemRepository.findByUserOrderByCreatedAtDesc(user)));
    }

    @DeleteMapping("/{userId}/products/{productId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<ApiResponse> removeFromWishlist(@PathVariable String userId, @PathVariable String productId) {
        WishlistItem item = wishlistItemRepository.findByUserUserIdAndProductProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found !!"));
        wishlistItemRepository.delete(item);

        ApiResponse response = ApiResponse.builder()
                .message("Product removed from wishlist !!")
                .status(HttpStatus.OK)
                .successs(true)
                .build();
        return ResponseEntity.ok(response);
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in database !!"));
    }

    private Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in database !!"));
    }

    private List<ProductDto> toProductDtos(List<WishlistItem> items) {
        return items.stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDto.class))
                .toList();
    }
}
