package com.oms.core.repository;

import com.oms.core.entity.Order;
import com.oms.core.constants.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findAllByOrderByOrderDateDesc();
    
    // Logistical Query: Fetch all tasks currently assigned to a specific delivery boy matching a list of active statuses
    List<Order> findByDeliveryBoyIdAndStatusInOrderByOrderDateDesc(Long deliveryBoyId, List<OrderStatus> statuses);
}