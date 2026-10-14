package com.mystreet.service;

import com.mystreet.dto.LoginRequestDto;
import com.mystreet.dto.RegisterRequestDto;
import com.mystreet.entity.User;
import com.mystreet.exception.UnauthorizedException;
import com.mystreet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@mystreet.com");
        dto.setPassword("password123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = authService.register(dto);
        assertNotNull(user);
        assertEquals("test@mystreet.com", user.getEmail());
    }

    @Test
    void testAuthenticate_Success() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("test@mystreet.com");
        dto.setPassword("password123");

        User existingUser = new User();
        existingUser.setEmail("test@mystreet.com");
        existingUser.setPasswordHash("encodedHash");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(dto.getPassword(), "encodedHash")).thenReturn(true);
        when(request.getSession(true)).thenReturn(session);

        User authenticated = authService.authenticate(dto, request);
        assertNotNull(authenticated);
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("test@mystreet.com");
        dto.setPassword("wrongpassword");

        User existingUser = new User();
        existingUser.setEmail("test@mystreet.com");
        existingUser.setPasswordHash("encodedHash");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(dto.getPassword(), "encodedHash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.authenticate(dto, request));
    }
}