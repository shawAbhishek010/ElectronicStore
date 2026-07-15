package com.lcwd.electronicStore.ElectronicStore.repositories;

import com.lcwd.electronicStore.ElectronicStore.entities.ProductView;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    List<ProductView> findTop20ByUserOrderByViewedAtDesc(User user);

    Optional<ProductView> findByUserUserIdAndProductProductId(String userId, String productId);
}
