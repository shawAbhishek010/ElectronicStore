package com.lcwd.electronicStore.ElectronicStore.dtos;
/*
Purpose:
Transfers cart details, totals, and cart items to the frontend.
*/
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDto {

    private String cartId;
    private LocalDateTime createdAt;
    private UserDto user;
    private List<CartItemDto> items = new ArrayList<>();
}
