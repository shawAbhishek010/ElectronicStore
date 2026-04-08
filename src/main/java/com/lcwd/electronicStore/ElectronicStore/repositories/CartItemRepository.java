package com.lcwd.electronicStore.ElectronicStore.repositories;

import com.lcwd.electronicStore.ElectronicStore.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
}