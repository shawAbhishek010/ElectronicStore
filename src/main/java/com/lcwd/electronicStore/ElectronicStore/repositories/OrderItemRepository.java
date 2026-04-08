package com.lcwd.electronicStore.ElectronicStore.repositories;

import com.lcwd.electronicStore.ElectronicStore.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer>
{
}
