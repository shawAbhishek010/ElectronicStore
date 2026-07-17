package com.lcwd.electronicStore.ElectronicStore.repositories;

/*
Purpose:
Provides database access for orders and user/payment-status lookups.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.Order;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUser(User user);

    Optional<Order> findFirstByUserAndPaymentStatusInOrderByOrderedDateDesc(
            User user,
            List<String> paymentStatuses
    );

}
