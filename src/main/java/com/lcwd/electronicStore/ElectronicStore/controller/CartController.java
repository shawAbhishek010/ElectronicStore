package com.lcwd.electronicStore.ElectronicStore.controller;


/*
Purpose:
Exposes user cart APIs for adding, updating, removing, clearing, and viewing cart items.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.AddItemToCartRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.CartDto;
import com.lcwd.electronicStore.ElectronicStore.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    //add items to cart
    @PostMapping("/{userId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<CartDto> addItemToCart(@PathVariable String userId, @RequestBody AddItemToCartRequest request) {
        CartDto cartDto = cartService.addItemToCart(userId, request);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }
    //delete item from Cart
    @DeleteMapping("/{userId}/items/{itemId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable String userId, @PathVariable int itemId) {
        cartService.removeItemFromCart(userId, itemId);
        ApiResponse response = ApiResponse.builder()
                .message("Item is removed !!")
                .successs(true)
                .status(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PatchMapping("/{userId}/items/{itemId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<CartDto> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable int itemId,
            @RequestBody AddItemToCartRequest request
    ) {
        CartDto cartDto = cartService.updateItemQuantity(userId, itemId, request.getQuantity());
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    //clear cart
    @DeleteMapping("/{userId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        ApiResponse response = ApiResponse.builder()
                .message("Now cart is blank !!")
                .successs(true)
                .status(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    //fetch cart information
    @GetMapping("/{userId}")
    @PreAuthorize("@securityGuard.isCurrentUserId(#userId)")
    public ResponseEntity<CartDto> getCart(@PathVariable String userId) {
        CartDto cartDto = cartService.getCartByUser(userId);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

}

