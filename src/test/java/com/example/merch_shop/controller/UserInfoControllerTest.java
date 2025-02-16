package com.example.merch_shop.controller;

import com.example.merch_shop.dto.InfoResponse;
import com.example.merch_shop.dto.InfoResponse.CoinHistory;
import com.example.merch_shop.dto.InfoResponse.InventoryItem;
import com.example.merch_shop.dto.InfoResponse.ReceivedCoinOperation;
import com.example.merch_shop.dto.InfoResponse.SentCoinOperation;
import com.example.merch_shop.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserInfoController.class)
@WithMockUser(username = "testuser")
class UserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getInfo_Success() throws Exception {
        InventoryItem item1 = InventoryItem.builder().type("t-shirt").quantity(1).build();
        InventoryItem item2 = InventoryItem.builder().type("cup").quantity(2).build();

        ReceivedCoinOperation received = ReceivedCoinOperation.builder().fromUser("otherUser").amount(50).build();
        SentCoinOperation sent = SentCoinOperation.builder().toUser("targetUser").amount(20).build();

        CoinHistory coinHistory = CoinHistory.builder()
                .received(List.of(received))
                .sent(List.of(sent))
                .build();

        InfoResponse mockResponse = InfoResponse.builder()
                .coins(100)
                .inventory(List.of(item1, item2))
                .coinHistory(coinHistory)
                .build();

        when(userService.getUserInfo("testuser"))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(100))
                .andExpect(jsonPath("$.inventory[0].type").value("t-shirt"))
                .andExpect(jsonPath("$.inventory[0].quantity").value(1))
                .andExpect(jsonPath("$.coinHistory.received[0].fromUser").value("otherUser"))
                .andExpect(jsonPath("$.coinHistory.received[0].amount").value(50))
                .andExpect(jsonPath("$.coinHistory.sent[0].toUser").value("targetUser"))
                .andExpect(jsonPath("$.coinHistory.sent[0].amount").value(20));
    }

    @Test
    void getInfo_UserNotFound() throws Exception {
        when(userService.getUserInfo("testuser"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("User not found"));
    }

    @Test
    void getInfo_InternalError() throws Exception {
        when(userService.getUserInfo("testuser"))
                .thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(get("/api/info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Internal server error"));
    }
}
