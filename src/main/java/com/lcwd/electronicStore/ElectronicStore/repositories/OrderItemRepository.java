package com.lcwd.electronicStore.ElectronicStore.repositories;

/*
Purpose:
Provides database access for order item rows.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer>
{
}
