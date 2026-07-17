package com.lcwd.electronicStore.ElectronicStore.entities;
/*
Purpose:
Represents one product line inside a user's cart.
*/
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_item_cart_product",
                columnNames = {"cart_id", "product_id"}
        )
)
public class CartItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cartItemId;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private  int quantity;

    @Column(columnDefinition = "BIGINT")
    private long totalPrice;
    //    mapping cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private  Cart cart;



}
