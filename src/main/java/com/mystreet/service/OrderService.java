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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public Order createOrder(UUID userId, OrderRequestDto orderRequestDto) {
        // 1. Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

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
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequestDto.getProductId()));

            // Check if sufficient stock is available
            if (product.getStockQty() < itemRequestDto.getQuantity()) {
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
        return orderRepository.save(order);
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        // Validate user existence
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Fetch and return orders for the user
        return orderRepository.findByUserId(userId);
    }

}
