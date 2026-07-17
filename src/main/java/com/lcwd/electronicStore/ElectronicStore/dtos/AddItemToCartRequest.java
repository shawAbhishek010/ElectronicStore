package com.lcwd.electronicStore.ElectronicStore.dtos;

/*
Purpose:
Carries product id and quantity when a user adds or updates a cart item.
*/
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddItemToCartRequest {

    private  String productId;

    private  int quantity;

}
