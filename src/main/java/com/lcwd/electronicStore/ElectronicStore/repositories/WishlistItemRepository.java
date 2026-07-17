package com.lcwd.electronicStore.ElectronicStore.repositories;

/*
Purpose:
Provides database access for wishlist items and user/product wishlist checks.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.entities.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserOrderByCreatedAtDesc(User user);

    Optional<WishlistItem> findByUserUserIdAndProductProductId(String userId, String productId);

    boolean existsByUserUserIdAndProductProductId(String userId, String productId);
}
