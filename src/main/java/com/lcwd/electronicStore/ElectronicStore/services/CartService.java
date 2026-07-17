package com.lcwd.electronicStore.ElectronicStore.services;

/*
Purpose:
Defines cart business operations used by cart controllers.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.AddItemToCartRequest;
import com.lcwd.electronicStore.ElectronicStore.dtos.CartDto;

public interface CartService {

    //add items to cart:
    //case1: cart for user is not available: we will create the cart and then add the item
    //case2: cart available add the items to cart
    CartDto addItemToCart(String userId, AddItemToCartRequest request);

    //remove item from cart:
    void removeItemFromCart(String userId,int cartItem);

    CartDto updateItemQuantity(String userId, int cartItem, int quantity);

    //remove all items from cart
    void clearCart(String userId);

    CartDto getCartByUser(String userId);
}
