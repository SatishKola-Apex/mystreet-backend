package com.mystreet.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.repository.ProductRepository;
import com.mystreet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Initializing MyStreet database setup...");

            // Seed Admin User if not exists
            if (userRepository.findByEmail("admin@mystreet.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@mystreet.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setAdmin(true);
                userRepository.save(admin);
                log.info("Default admin user created successfully: admin@mystreet.com");
            }
            else {
                log.info("Admin user already exists. Skipping creation.");
            }

            // Seed Products from JSON file if table is empty
            if (productRepository.count() == 0) {
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<List<Product>> typeRef = new TypeReference<>() {};

                try {
                    ClassPathResource resource = new ClassPathResource("products.json");
                    try (InputStream inputStream = resource.getInputStream()) {
                        List<Product> products = mapper.readValue(inputStream, typeRef);
                        productRepository.saveAll(products);
                        log.info("Successfully seeded {} sneaker products into the database!", products.size());
                    }
                } catch (Exception e) {
                    log.error("Failed to seed products from JSON file: {}", e.getMessage(), e);
                }
            } else {
                log.info("Product Catalog already initialized. Total products: {}", productRepository.count());
            }
        };
    }
}