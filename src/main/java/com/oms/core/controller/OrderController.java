package com.oms.core.controller;

import com.oms.core.constants.AppRole;
import com.oms.core.constants.OrderStatus;
import com.oms.core.dto.request.OrderUpdateDto;
import com.oms.core.entity.Order;
import com.oms.core.entity.OrderItem;
import com.oms.core.entity.Product;
import com.oms.core.entity.Role;
import com.oms.core.entity.User;
import com.oms.core.repository.OrderRepository;
import com.oms.core.repository.ProductRepository;
import com.oms.core.repository.RoleRepository;
import com.oms.core.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OrderController(OrderRepository orderRepository, 
                           ProductRepository productRepository, 
                           UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping
    public ResponseEntity<?> placeOrderBooking(@RequestBody Order incomingOrder) {
        if (incomingOrder.getUser() == null || incomingOrder.getUser().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid user ID reference correlation."));
        }

        incomingOrder.setTrackingNumber("ORD" + System.currentTimeMillis() / 1000);
        incomingOrder.setStatus(OrderStatus.PENDING);
        incomingOrder.setOrderDate(LocalDateTime.now());

        double runningTotalAccumulator = 0.0;

        if (incomingOrder.getOrderItems() != null) {
            List<OrderItem> targetedItems = incomingOrder.getOrderItems();
            incomingOrder.setOrderItems(new ArrayList<>());
            
            for (OrderItem item : targetedItems) {
                Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product context item dropped out of catalog."));
                
                double calculatedItemPrice = product.getPrice().doubleValue();
                item.setProduct(product);
                item.setPriceAtPurchase(calculatedItemPrice);
                
                runningTotalAccumulator += (calculatedItemPrice * item.getQuantity());
                
                incomingOrder.addOrderItem(item);
            }
        }

        incomingOrder.setTotalAmount(runningTotalAccumulator);
        Order completeSavedOrder = orderRepository.save(incomingOrder);
        return ResponseEntity.ok(completeSavedOrder);
    }

    // Admin: Get all orders across the entire database history (PENDING, ASSIGNED, OUT_FOR_DELIVERY, PLACED, CANCELLED)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllSystemOrdersMasterLog() {
        return ResponseEntity.ok(orderRepository.findAllByOrderByOrderDateDesc());
    }

    // User Dashboard: Fetch full history across ALL tracking states without excluding logistical transitions
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('DELIVERY')")
    public ResponseEntity<List<Order>> getCompleteHistoryForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByOrderDateDesc(userId));
    }

    // Shared View Details Endpoint: Expanded authority so Delivery Boy can look up address summaries on assignment
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('DELIVERY')")
    public ResponseEntity<Order> getIndividualOrderSummary(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Order statement tracking reference unverified."));
        return ResponseEntity.ok(order);
    }

    // User Endpoint: Modify delivery address data or identity attributes on active orders
    @PutMapping("/{id}/metadata")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderShippingMetadata(@PathVariable Long id, @Valid @RequestBody OrderUpdateDto updateDetails) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Target Order entry missing."));

        if (order.getStatus() != OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "Locked State: Transformed orders cannot accept metadata edits."));
        }

        order.setCustomerFullName(updateDetails.customerFullName());
        order.setPhoneNumber(updateDetails.phoneNumber());
        order.setShippingAddress(updateDetails.shippingAddress());

        return ResponseEntity.ok(orderRepository.save(order));
    }

    // Admin fallback method: Force-finalize state lifecycle manually to PLACED
    @PutMapping("/place/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> markOrderAsPlaced(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Order target missing."));
        order.setStatus(OrderStatus.PLACED);
        return ResponseEntity.ok(orderRepository.save(order));
    }

    // Shared: Transition status value to CANCELLED
    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrderLifecycle(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Order target missing."));
        order.setStatus(OrderStatus.CANCELLED);
        return ResponseEntity.ok(orderRepository.save(order));
    }

    // Logistical: Fetch all available delivery personnel profiles (For Admin dropdown menus)
    @GetMapping("/delivery-boys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllActiveDeliveryPersonnel() {
        Role deliveryRole = roleRepository.findByName(AppRole.ROLE_DELIVERY)
                .orElseThrow(() -> new RuntimeException("Error: Delivery role registry missing."));
        return ResponseEntity.ok(userRepository.findByRolesContaining(deliveryRole));
    }

    // Logistical: Assign a delivery boy to an incoming unassigned order
    @PutMapping("/{id}/assign/{deliveryBoyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignDeliveryBoyToOrder(@PathVariable Long id, @PathVariable Long deliveryBoyId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Target order missing."));
        User deliveryBoy = userRepository.findById(deliveryBoyId)
                .orElseThrow(() -> new RuntimeException("Error: Selected delivery profile invalid."));

        order.setDeliveryBoy(deliveryBoy);
        order.setStatus(OrderStatus.ASSIGNED); // Shifts status from PENDING to ASSIGNED
        
        return ResponseEntity.ok(orderRepository.save(order));
    }

    // Logistical: Fetch all current ongoing delivery tasks belonging specifically to one delivery boy ID
    @GetMapping("/delivery/{deliveryBoyId}/active")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<Order>> getActiveLogisticalTasks(@PathVariable Long deliveryBoyId) {
        return ResponseEntity.ok(orderRepository.findByDeliveryBoyIdAndStatusInOrderByOrderDateDesc(
                deliveryBoyId, 
                List.of(OrderStatus.ASSIGNED, OrderStatus.OUT_FOR_DELIVERY)
        ));
    }

    // Logistical: Update order delivery state (Out for Delivery -> Placed/Delivered)
    @PutMapping("/{id}/delivery-status")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<?> updateLogisticalDeliveryState(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Order statement reference invalid."));

        if (status != OrderStatus.OUT_FOR_DELIVERY && status != OrderStatus.PLACED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid logistical operational transition."));
        }

        order.setStatus(status); // If status is PLACED, it locks cash delivery collection and logs to history
        return ResponseEntity.ok(orderRepository.save(order));
    }
}