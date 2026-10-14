package com.mystreet.controller;

import com.mystreet.dto.LoginRequestDto;
import com.mystreet.dto.RegisterRequestDto;
import com.mystreet.entity.User;
import com.mystreet.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and logout")
public class AuthController {

    public final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new customer or admin user with the provided details.")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        User savedUser = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user credentials and returns a success message if valid.")
    @ApiResponse(responseCode = "200", description = "User logged in successfully")
    public ResponseEntity<User> login(@Valid @RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
        User user = authService.authenticate(loginRequest, request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Logs out the currently authenticated user.")
    @ApiResponse(responseCode = "204", description = "User logged out successfully")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}