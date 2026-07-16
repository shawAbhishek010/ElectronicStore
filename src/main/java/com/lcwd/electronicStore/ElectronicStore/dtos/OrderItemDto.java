package com.lcwd.electronicStore.ElectronicStore.dtos;


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
