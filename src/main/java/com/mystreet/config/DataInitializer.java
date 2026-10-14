package com.mystreet.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystreet.entity.Product;
import com.mystreet.entity.User;
import com.mystreet.repository.ProductRepository;
import com.mystreet.repository.UserRepository;
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

    @Bean
    @Transactional
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Admin User if not exists
            if (userRepository.findByEmail("admin@mystreet.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@mystreet.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setAdmin(true);
                userRepository.save(admin);
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
                        System.out.println("Successfully seeded " + products.size() + " products into the database!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}