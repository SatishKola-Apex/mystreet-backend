package com.mystreet.service;

import com.mystreet.dto.OrderItemsRequestDto;
import com.mystreet.dto.OrderRequestDto;
import com.mystreet.entity.Order;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.exception.ResourceNotFoundException;
import com.mystreet.repository.OrderRepository;
import com.mystreet.repository.ProductRepository;
import com.mystreet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOrderById_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order found = orderService.getOrderById(orderId);
        assertNotNull(found);
    }

    @Test
    void testGetOrderById_NotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void testGetOrdersByUserId_Success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(orderRepository.findByUserId(userId)).thenReturn(Arrays.asList(new Order(), new Order()));

        List<Order> orders = orderService.getOrdersByUserId(userId);
        assertEquals(2, orders.size());
    }

    @Test
    void testCreateOrder_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        Product product = new Product();
        product.setStockQty(10);
        product.setPrice(new BigDecimal("1000"));

        OrderItemsRequestDto itemDto = new OrderItemsRequestDto();
        itemDto.setProductId(UUID.randomUUID());
        itemDto.setQuantity(2);
        itemDto.setSelectedSize("9");

        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setShippingAddress("123 Street");
        requestDto.setOrderItems(Arrays.asList(itemDto));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(itemDto.getProductId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order createdOrder = orderService.createOrder(userId, requestDto);

        assertNotNull(createdOrder);
        assertEquals("123 Street", createdOrder.getShippingAddress());
        assertEquals(new BigDecimal("2000"), createdOrder.getTotalAmount());
        assertEquals(8, product.getStockQty()); // Verify stock deduction
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}