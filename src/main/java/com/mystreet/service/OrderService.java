package com.mystreet.service;

import com.mystreet.dto.OrderItemsRequestDto;
import com.mystreet.dto.OrderRequestDto;
import com.mystreet.entity.Order;
import com.mystreet.entity.OrderItem;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.exception.ResourceNotFoundException;
import com.mystreet.repository.OrderRepository;
import com.mystreet.repository.ProductRepository;
import com.mystreet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(UUID userId, OrderRequestDto orderRequestDto) {
        log.info("Processing order creation for User ID: {}", userId);

        // 1. Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Order creation failed - User not found with ID: {}", userId);
                    return new RuntimeException("User not found with id: " + userId);
                });

        // 2. Initialize the parent Order entity
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(orderRequestDto.getShippingAddress());
        order.setStatus("PLACED");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Process each item inside the request DTO items list
        for (OrderItemsRequestDto itemRequestDto : orderRequestDto.getOrderItems()) {
            Product product = productRepository.findById(itemRequestDto.getProductId())
                    .orElseThrow(() -> {
                        log.warn("Order creation failed - Product not found with ID: {}", itemRequestDto.getProductId());
                        return new ResourceNotFoundException("Product not found with id: " + itemRequestDto.getProductId());
                    });

            // Check if sufficient stock is available
            if (product.getStockQty() < itemRequestDto.getQuantity()) {
                log.warn("Order creation failed - Insufficient stock for product '{}' (Requested: {}, Available: {}", product.getName(), itemRequestDto.getQuantity(), product.getStockQty());
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Deduct the ordered quantity from the product's stock
            product.setStockQty(product.getStockQty() - itemRequestDto.getQuantity());
            productRepository.save(product);

            // Calculate the price point subtotal for this item row
            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequestDto.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);

            //Build the child OrderItem entity and associate it with the parent Order entity
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequestDto.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setSelectedSize(itemRequestDto.getSelectedSize());

            orderItems.add(orderItem);
        }

        // 4. Attach calculations and items collection list to the parent Order entity
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        // 5. Save the parent Order entity, which will cascade and save the child OrderItem entities
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with ID: {} for user ID: {} | Total Amount: ₹{}", savedOrder.getId(), userId, totalAmount);

        return savedOrder;
    }

    public Order getOrderById(UUID orderId) {
        log.info("Fetching order details for order ID: {}", orderId);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order lookup failed - Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        log.info("Fetching order history for user ID: {}", userId);

        // Validate user existence
        if (!userRepository.existsById(userId)) {
            log.warn("Order histry lookup failed - User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Fetch and return orders for the user
        List<Order> userOrders = orderRepository.findByUserId(userId);
        log.info("Retrieved {} past orders for User ID: {}", userOrders.size());
        return userOrders;
    }

}
