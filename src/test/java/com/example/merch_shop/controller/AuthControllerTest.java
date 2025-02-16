package com.example.merch_shop.controller;

import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.UserRepository;
import com.example.merch_shop.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepo;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void auth_SuccessfulAuthentication() throws Exception {
        when(authManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("user", null));
        when(tokenService.generateToken(any())).thenReturn("test-token");

        mockMvc.perform(post("/api/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void auth_BadCredentialsWithExistingUser_ShouldReturnConflict() throws Exception {
        when(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException.class);
        when(userRepo.existsByUsername("existing")).thenReturn(true);

        mockMvc.perform(post("/api/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"existing\",\"password\":\"wrong\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors").value("Username already exists"));
    }

    @Test
    void auth_BadCredentialsWithNewUser_ShouldRegister() throws Exception {
        when(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException.class)
                .thenReturn(new UsernamePasswordAuthenticationToken("newuser", null));
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-pass");
        when(tokenService.generateToken(any())).thenReturn("new-token");

        mockMvc.perform(post("/api/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"newpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));

        verify(userRepo).saveAndFlush(any(User.class));
    }

    @Test
    void auth_RegistrationConflict_ShouldReturnConflict() throws Exception {
        when(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException.class);
        when(userRepo.existsByUsername("duplicate")).thenReturn(false);
        when(userRepo.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate username"));

        mockMvc.perform(post("/api/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"duplicate\",\"password\":\"pass\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors").value("Username already exists"));
    }

    @Test
    void auth_RegistrationFailure_ShouldReturnInternalError() throws Exception {
        when(authManager.authenticate(any()))
                .thenThrow(BadCredentialsException.class);
        when(userRepo.existsByUsername("erroruser")).thenReturn(false);
        when(userRepo.save(any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"erroruser\",\"password\":\"pass\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").value("Registration failed"));
    }
}