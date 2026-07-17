package com.lcwd.electronicStore.ElectronicStore.entities;

/*
Purpose:
Represents a sellable catalog product with pricing, stock, image, and category data.
*/
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {


    @Id
    private String productId;
    private String title;
    @Column(length = 10000)
    private String description;
    @Column(columnDefinition = "BIGINT")
    private long price;

    @Column(columnDefinition = "BIGINT")
    private long discountedPrice;
    private int quantity;
    private LocalDateTime addedDate;
    // Indicates whether product is visible/active for users
    private boolean live;

    // Indicates whether product is available in inventory
    private boolean stock;
    @Column(length = 1000)
    private String productImageName;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private  Category category;
}
