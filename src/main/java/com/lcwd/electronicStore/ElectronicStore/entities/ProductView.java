package com.lcwd.electronicStore.ElectronicStore.entities;

/*
Purpose:
Stores a user's product view history for recently viewed recommendations.
*/
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "product_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productViewId;

    private LocalDateTime viewedAt;

    private int viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
