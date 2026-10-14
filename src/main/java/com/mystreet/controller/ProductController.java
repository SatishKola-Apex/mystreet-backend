package com.mystreet.controller;

import com.mystreet.dto.ProductRequestDto;
import com.mystreet.entity.Product;
import com.mystreet.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Endpoints for managing products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a list of all available products with optional filtering by brand and size.")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String size) {
        List<Product> products = productService.getFilteredProducts(brand, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a product by its unique identifier.")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product to the catalog with the provided details.")
    public ResponseEntity<Product> createProduct(
            @RequestParam UUID userId,
            @Valid @RequestBody ProductRequestDto productDto) {
        Product createdProduct = productService.createProduct(userId, productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product's details by its unique identifier.")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @RequestParam UUID userId,
            @Valid @RequestBody ProductRequestDto productDto) {
        Product updatedProduct = productService.updateProduct(userId, id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product from the catalog by its unique identifier.")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        productService.deleteProduct(userId, id);
        return ResponseEntity.noContent().build();
    }
}
