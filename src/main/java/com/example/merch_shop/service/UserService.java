package com.example.merch_shop.service;

import com.example.merch_shop.dto.InfoResponse;
import com.example.merch_shop.model.User;
import com.example.merch_shop.model.CoinTransfer;
import com.example.merch_shop.model.Purchase;
import com.example.merch_shop.repository.UserRepository;
import com.example.merch_shop.repository.PurchaseRepository;
import com.example.merch_shop.repository.CoinTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final CoinTransferRepository coinTransferRepository;

    @Transactional(readOnly = true)
    public InfoResponse getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new RuntimeException("User not found");
                });

        return InfoResponse.builder()
                .coins(user.getCoins())
                .inventory(getInventory(user))
                .coinHistory(getCoinHistory(user))
                .build();
    }

    private List<InfoResponse.InventoryItem> getInventory(User user) {
        return purchaseRepository.getInventory(user).stream()
                .map(projection -> InfoResponse.InventoryItem.builder()
                        .type(projection.getType())
                        .quantity(projection.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private InfoResponse.CoinHistory getCoinHistory(User user) {
        List<CoinTransfer> transfers = coinTransferRepository.findBySenderOrReceiver(user, user);

        List<InfoResponse.ReceivedCoinOperation> received = transfers.stream()
                .filter(t -> t.getReceiver().equals(user))
                .map(t -> InfoResponse.ReceivedCoinOperation.builder()
                        .fromUser(t.getSender().getUsername())
                        .amount(t.getAmount())
                        .build())
                .collect(Collectors.toList());

        List<InfoResponse.SentCoinOperation> sent = transfers.stream()
                .filter(t -> t.getSender().equals(user))
                .map(t -> InfoResponse.SentCoinOperation.builder()
                        .toUser(t.getReceiver().getUsername())
                        .amount(t.getAmount())
                        .build())
                .collect(Collectors.toList());

        return InfoResponse.CoinHistory.builder()
                .received(received)
                .sent(sent)
                .build();
    }
}