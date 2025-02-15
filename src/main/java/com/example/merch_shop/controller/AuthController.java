package com.example.merch_shop.controller;

import com.example.merch_shop.dto.AuthRequest;
import com.example.merch_shop.dto.AuthResponse;
import com.example.merch_shop.dto.ErrorResponse;
import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.UserRepository;
import com.example.merch_shop.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthController {
    private final TokenService tokenService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(TokenService tokenService, AuthenticationManager authManager,
                          UserRepository userRepo, PasswordEncoder encoder) {
        this.tokenService = tokenService;
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/api/auth")
    @Transactional
    public ResponseEntity<?> auth(@RequestBody AuthRequest request) {
        log.info("Auth request for user: {}", request.username());

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            log.info("Successful authentication for: {}", request.username());
            return ResponseEntity.ok(new AuthResponse(tokenService.generateToken(auth)));

        } catch (BadCredentialsException ex) {
            log.warn("Bad credentials for: {}", request.username());
            return processRegistration(request);
        }
    }

    private ResponseEntity<?> processRegistration(AuthRequest request) {
        log.info("Attempting registration for: {}", request.username());

        if (userRepo.existsByUsername(request.username())) {
            log.error("Username conflict: {}", request.username());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Username already exists"));
        }

        try {
            User newUser = new User();
            newUser.setUsername(request.username());
            newUser.setPassword(encoder.encode(request.password()));
            userRepo.saveAndFlush(newUser);

            log.info("New user registered: {}", request.username());

            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            return ResponseEntity.ok(new AuthResponse(tokenService.generateToken(auth)));

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Username already exists"));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed"));
        }
    }
}