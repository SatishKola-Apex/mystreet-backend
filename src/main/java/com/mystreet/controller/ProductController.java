package com.mystreet.controller;

import com.mystreet.dto.ProductRequestDto;
import com.mystreet.entity.Product;
import com.mystreet.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String size) {
        List<Product> products = productService.getFilteredProducts(brand, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestParam UUID userId,
            @Valid @RequestBody ProductRequestDto productDto) {
        Product createdProduct = productService.createProduct(userId, productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @RequestParam UUID userId,
            @Valid @RequestBody ProductRequestDto productDto) {
        Product updatedProduct = productService.updateProduct(userId, id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        productService.deleteProduct(userId, id);
        return ResponseEntity.noContent().build();
    }
}
