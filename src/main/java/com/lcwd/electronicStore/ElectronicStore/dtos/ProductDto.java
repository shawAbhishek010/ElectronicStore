package com.lcwd.electronicStore.ElectronicStore.dtos;

/*
Purpose:
Transfers product catalog, pricing, inventory, and category data.
*/
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductDto {
    private String productId;
    private String title;
    private String description;
    private long price;
    private long discountedPrice;
    private int quantity;
    private LocalDateTime addedDate;
    private boolean live;
    private boolean stock;
    private String productImageName;
    private CategoryDto category;




}
