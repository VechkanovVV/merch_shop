package com.example.merch_shop.controller;

import com.example.merch_shop.model.CoinTransfer;
import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.CoinTransferRepository;
import com.example.merch_shop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SendCoinController.class)
@WithMockUser(username = "senderUser")
class SendCoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CoinTransferRepository coinTransferRepository;

    @Test
    void sendCoin_Success() throws Exception {
        User sender = new User();
        sender.setUsername("senderUser");
        sender.setCoins(100);

        User receiver = new User();
        receiver.setUsername("receiverUser");
        receiver.setCoins(50);

        when(userRepository.findByUsername("senderUser"))
                .thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("receiverUser"))
                .thenReturn(Optional.of(receiver));

        mockMvc.perform(post("/api/sendCoin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"receiverUser\",\"amount\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Coins transferred successfully"));

        verify(userRepository).save(sender);
        verify(userRepository).save(receiver);
        verify(coinTransferRepository).save(any(CoinTransfer.class));
    }

    @Test
    void sendCoin_InsufficientCoins() throws Exception {
        User sender = new User();
        sender.setUsername("senderUser");
        sender.setCoins(20);

        when(userRepository.findByUsername("senderUser"))
                .thenReturn(Optional.of(sender));

        mockMvc.perform(post("/api/sendCoin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"receiverUser\",\"amount\":30}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Insufficient coins balance"));

        verify(userRepository, never()).save(any());
        verify(coinTransferRepository, never()).save(any());
    }

    @Test
    void sendCoin_SenderNotFound() throws Exception {
        when(userRepository.findByUsername("senderUser"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/sendCoin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"receiverUser\",\"amount\":30}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").value("User not found: senderUser"));

        verify(userRepository, never()).save(any());
        verify(coinTransferRepository, never()).save(any());
    }

    @Test
    void sendCoin_ReceiverNotFound() throws Exception {
        User sender = new User();
        sender.setUsername("senderUser");
        sender.setCoins(100);

        when(userRepository.findByUsername("senderUser"))
                .thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("receiverUser"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/sendCoin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"receiverUser\",\"amount\":30}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").value("Receiver  not found: receiverUser"));

        verify(userRepository, never()).save(any());
        verify(coinTransferRepository, never()).save(any());
    }

    @Test
    void sendCoin_InternalServerError() throws Exception {
        User sender = new User();
        sender.setUsername("senderUser");
        sender.setCoins(100);

        when(userRepository.findByUsername("senderUser"))
                .thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("receiverUser"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/sendCoin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"receiverUser\",\"amount\":30}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").value("Internal server error"));

        verify(userRepository, never()).save(any());
        verify(coinTransferRepository, never()).save(any());
    }
}