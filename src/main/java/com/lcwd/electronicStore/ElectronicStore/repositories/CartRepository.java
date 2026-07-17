package com.lcwd.electronicStore.ElectronicStore.repositories;

/*
Purpose:
Provides database access for carts and lookup by owning user.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.Cart;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUser(User user);

}
