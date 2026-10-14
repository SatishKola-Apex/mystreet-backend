package com.mystreet.controller;

import com.mystreet.dto.ProductRequestDto;
import com.mystreet.entity.Product;
import com.mystreet.service.ProductService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetProductsEndpoint() throws Exception {
        Product p = new Product();
        when(productService.getFilteredProducts(null, null)).thenReturn(Arrays.asList(p));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testCreateProductEndpoint() throws Exception {
        UUID adminId = UUID.randomUUID();
        ProductRequestDto dto = new ProductRequestDto();
        dto.setName("Air Max");
        dto.setBrand("Nike");
        dto.setPrice(new BigDecimal("2000"));
        dto.setImageUrl("url");
        dto.setStockQty(12);
        dto.setSizesCsv("6,7,8");

        Product savedProduct = new Product();
        savedProduct.setName("Air Max");

        when(productService.createProduct(eq(adminId), any(ProductRequestDto.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/products?userId=" + adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Air Max"));
    }

    @Test
    void testDeleteProductEndpoint() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        doNothing().when(productService).deleteProduct(adminId, productId);

        mockMvc.perform(delete("/api/products/" + productId + "?userId=" + adminId))
                .andExpect(status().isNoContent());
    }
}