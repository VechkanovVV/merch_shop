package com.example.merch_shop.controller;

import com.example.merch_shop.exception.InsufficientCoinsException;
import com.example.merch_shop.exception.ProductNotFoundException;
import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.UserRepository;
import com.example.merch_shop.service.ShopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
@WithMockUser(username = "testuser")
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShopService shopService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void buyItem_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setCoins(100);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        doNothing().when(shopService).purchaseItem(any(), anyString());

        mockMvc.perform(get("/api/buy/item123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Purchase successful"))
                .andExpect(jsonPath("$.remainingCoins").value(100));

        verify(shopService).purchaseItem(user, "item123");
    }

    @Test
    void buyItem_ProductNotFound() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        doThrow(new ProductNotFoundException("Item not found"))
                .when(shopService).purchaseItem(any(), anyString());

        mockMvc.perform(get("/api/buy/invalidItem")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));

        verify(shopService).purchaseItem(user, "invalidItem");
    }

    @Test
    void buyItem_InsufficientCoins() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        doThrow(new InsufficientCoinsException("Not enough coins"))
                .when(shopService).purchaseItem(any(), anyString());

        mockMvc.perform(get("/api/buy/expensiveItem")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Not enough coins"));
    }

    @Test
    void buyItem_UserNotFound() throws Exception {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/buy/item123")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found: testuser"));
    }
}