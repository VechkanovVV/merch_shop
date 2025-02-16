package com.example.merch_shop.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class InfoResponse {
    private int coins;
    private List<InventoryItem> inventory;
    private CoinHistory coinHistory;

    @Getter
    @Setter
    @Builder
    public static class InventoryItem {
        private String type;
        private int quantity;
    }

    @Getter
    @Setter
    @Builder
    public static class CoinHistory {
        private List<ReceivedCoinOperation> received;
        private List<SentCoinOperation> sent;
    }

    @Getter
    @Setter
    @Builder
    public static class ReceivedCoinOperation {
        private String fromUser;
        private int amount;
    }

    @Getter
    @Setter
    @Builder
    public static class SentCoinOperation {
        private String toUser;
        private int amount;
    }
}