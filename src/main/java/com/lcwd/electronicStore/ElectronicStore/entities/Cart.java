package com.lcwd.electronicStore.ElectronicStore.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart")
public class Cart {

    @Id
    private String cartId;
    private LocalDateTime createdAt;
    @OneToOne
    private User user;
    //mapping cart-items
//    cascade = ALL ->>> When parent saved/deleted → child affected
//    orphanRemoval = true ->>>When child removed from list → delete child
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
