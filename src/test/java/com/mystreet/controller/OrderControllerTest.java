package com.mystreet.controller;

import com.mystreet.dto.OrderItemsRequestDto;
import com.mystreet.dto.OrderRequestDto;
import com.mystreet.entity.Order;
import com.mystreet.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrderEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setShippingAddress("Test Address");
        requestDto.setPaymentMode("Online");

        OrderItemsRequestDto itemsdto = new OrderItemsRequestDto();
        itemsdto.setSelectedSize("6");
        itemsdto.setProductId(userId);
        itemsdto.setQuantity(2);
        requestDto.setOrderItems(Arrays.asList(itemsdto));

        Order savedOrder = new Order();
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setTotalAmount(new BigDecimal("2500"));

        when(orderService.createOrder(eq(userId), any(OrderRequestDto.class))).thenReturn(savedOrder);

        mockMvc.perform(post("/api/orders")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                        .andExpect(status().isCreated());
    }

    @Test
    void testGetMyOrdersEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();
        when(orderService.getOrdersByUserId(userId)).thenReturn(Arrays.asList(new Order()));

        mockMvc.perform(get("/api/orders/mine")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
}