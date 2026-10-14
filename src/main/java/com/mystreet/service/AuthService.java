package com.mystreet.service;

import com.mystreet.dto.LoginRequestDto;
import com.mystreet.dto.RegisterRequestDto;
import com.mystreet.entity.User;
import com.mystreet.exception.UnauthorizedException;
import com.mystreet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequestDto requestDto) {
        // Check if user with the same email already exists
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // Create a new user entity and set its properties
        User user = new User();
        user.setEmail(requestDto.getEmail());

        //Hash the password before saving it to the database
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));

        //Default isAdmin to false for new users (Handled in User entity, but explicitly setting it here for clarity)
        user.setAdmin(false);

        // Save the new user to the database
        userRepository.save(user);
        return user;
    }

    public User authenticate(LoginRequestDto requestDto, HttpServletRequest request) {
        // Find the user by email or throw unauthorized exception if not found
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Check if the provided password matches the stored password hash
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        ); // Create a new session if one doesn't exist

        // If authentication is successful, return the authenticated user
        return user;
    }

    public void logout() {
        // Clear the security context
        SecurityContextHolder.clearContext();
    }
}