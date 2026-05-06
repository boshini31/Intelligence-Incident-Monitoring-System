package com.incidents.service;

import com.incidents.dto.AuthDto;
import com.incidents.entity.User;
import com.incidents.exception.BadRequestException;
import com.incidents.repository.UserRepository;
import com.incidents.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login.
 *
 * Interview point: What is the AuthenticationManager?
 * - Spring Security's core interface for authenticating a user
 * - It delegates to our DaoAuthenticationProvider which:
 *   1. Loads user from DB via UserDetailsService
 *   2. Compares BCrypt hashed password
 *   3. Throws BadCredentialsException if wrong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        // Check for duplicate username/email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        // Create and save new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash the password!
                .email(request.getEmail())
                .role(User.Role.USER)  // Default role
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Generate JWT immediately so user can start using the API
        String token = jwtUtil.generateToken(user);

        return AuthDto.AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("User registered successfully")
                .build();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // This throws AuthenticationException if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If we reach here, authentication succeeded
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtUtil.generateToken(user);
        log.info("User logged in: {}", user.getUsername());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
