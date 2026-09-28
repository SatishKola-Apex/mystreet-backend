package com.mystreet.service;

import com.mystreet.dto.ProductRequestDto;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.exception.ResourceNotFoundException;
import com.mystreet.exception.UnauthorizedException;
import com.mystreet.repository.ProductRepository;
import com.mystreet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> getFilteredProducts(String brand, String size) {
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
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + id));
    }

    public Product createProduct(UUID userId, ProductRequestDto productRequestDto) {
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

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(UUID userId, UUID productId, ProductRequestDto productRequestDto) {
        // Enforce capstone admin restriction rules
        validateAdminAccess(userId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + productId));

        existingProduct.setName(productRequestDto.getName());
        existingProduct.setBrand(productRequestDto.getBrand());
        existingProduct.setDescription(productRequestDto.getDescription());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setImageUrl(productRequestDto.getImageUrl());
        existingProduct.setSizesCsv(productRequestDto.getSizesCsv()); //existingProduct.setSizesCsv(String.join(",", productRequestDto.getSizes()));
        existingProduct.setStockQty(productRequestDto.getStockQty());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(UUID userId, UUID productId) {// Enforce capstone admin restriction ruls
        // Enforce capstone admin restriction rules
        validateAdminAccess(userId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + productId));

        productRepository.delete(existingProduct);
    }

    private void validateAdminAccess(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + userId));

        if (!user.isAdmin()) {
            throw new UnauthorizedException("Access denied: User does not have admin privileges");
        }
    }
}
