package com.lcwd.electronicStore.ElectronicStore.repositories;

import com.lcwd.electronicStore.ElectronicStore.entities.Order;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUser(User user);

}