package com.lcwd.electronicStore.ElectronicStore.dtos;


/*
Purpose:
Transfers a purchased order item with product, quantity, and line total.
*/
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderItemDto {


    private int orderItemId;

    private int quantity;

    private long totalPrice;

    private ProductDto product;


}
