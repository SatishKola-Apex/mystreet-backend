package com.mystreet.service;

import com.mystreet.dto.ProductRequestDto;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.exception.ResourceNotFoundException;
import com.mystreet.exception.UnauthorizedException;
import com.mystreet.repository.ProductRepository;
import com.mystreet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> getFilteredProducts(String brand, String size) {
        log.info("Fetching products with filters - Brand: [{}], Size: [{}]", brand, size);
        if (brand != null &&!brand.isEmpty() && size != null && !size.isEmpty()) {
            return productRepository.findByBrandIgnoreCaseAndSizesCsvContaining(brand, size);
        } else if (brand != null && !brand.isEmpty()) {
            return productRepository.findByBrandIgnoreCase(brand);
        } else if (size != null && !size.isEmpty()) {
            return productRepository.findBySizesCsvContaining(size);
        } else {
            return productRepository.findAll();
        }
    }

    public Product getProductById(UUID id) {
        log.info("Fetching Product details for ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product lookup failed - Product not found with ID: {}", id);
                    return new ResourceNotFoundException("Product not found with id : " + id);
                });
    }

    @Transactional
    public Product createProduct(UUID userId, ProductRequestDto productRequestDto) {
        log.info("Admin action requested: Create product '{}' byuser ID: {}", productRequestDto.getName(), userId);
        // Enforce capstone admin restriction ruls
        validateAdminAccess(userId);

        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setBrand(productRequestDto.getBrand());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        product.setImageUrl(productRequestDto.getImageUrl());
        product.setSizesCsv(productRequestDto.getSizesCsv()); //product.setSizesCsv(String.join(",", productRequestDto.getSizes()));
        product.setStockQty(productRequestDto.getStockQty());

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {} ('{}')", savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(UUID userId, UUID productId, ProductRequestDto productRequestDto) {
        log.info("Admin action requested: Update product ID: {} by User ID: {}", productId, userId);

        // Enforce capstone admin restriction rules
        validateAdminAccess(userId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product update failed - Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found with id : " + productId);
                });

        existingProduct.setName(productRequestDto.getName());
        existingProduct.setBrand(productRequestDto.getBrand());
        existingProduct.setDescription(productRequestDto.getDescription());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setImageUrl(productRequestDto.getImageUrl());
        existingProduct.setSizesCsv(productRequestDto.getSizesCsv()); //existingProduct.setSizesCsv(String.join(",", productRequestDto.getSizes()));
        existingProduct.setStockQty(productRequestDto.getStockQty());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(UUID userId, UUID productId) {
        log.info("Admin action requested: Delete product ID: {} by user ID: {}", productId, userId);
        // Enforce admin restriction rules
        validateAdminAccess(userId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product deletion failed - Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found with id : " + productId);
                });

        productRepository.delete(existingProduct);
        log.info("roduct deleted successfully with ID: {}", productId);
    }

    private void validateAdminAccess(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Admin validation failed - User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with id : " + userId);
                });

        if (!user.isAdmin()) {
            log.warn("Access denied - User ID: {} does not have admin privileges", userId);
            throw new UnauthorizedException("Access denied: User does not have admin privileges");
        }
    }
}
