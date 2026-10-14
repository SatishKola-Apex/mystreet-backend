package com.mystreet.service;

import com.mystreet.dto.ProductRequestDto;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.exception.ResourceNotFoundException;
import com.mystreet.exception.UnauthorizedException;
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

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFilteredProducts_AllFiltersNull() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(new Product(), new Product()));
        List<Product> products = productService.getFilteredProducts(null, null);
        assertEquals(2, products.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_Success() {
        UUID id = UUID.randomUUID();
        Product product = new Product();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product found = productService.getProductById(id);
        assertNotNull(found);
    }

    @Test
    void testGetProductById_NotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(id));
    }

    @Test
    void testCreateProduct_AdminSuccess() {
        UUID adminId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setAdmin(true);

        ProductRequestDto dto = new ProductRequestDto();
        dto.setName("Sneaker X");
        dto.setBrand("Nike");
        dto.setPrice(new BigDecimal("1500"));

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product created = productService.createProduct(adminId, dto);
        assertNotNull(created);
        assertEquals("Sneaker X", created.getName());
    }

    @Test
    void testCreateProduct_NonAdminUnauthorized() {
        UUID userId = UUID.randomUUID();
        User normalUser = new User();
        normalUser.setAdmin(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        assertThrows(UnauthorizedException.class, () -> productService.createProduct(userId, new ProductRequestDto()));
    }
}