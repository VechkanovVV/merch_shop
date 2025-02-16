package com.example.merch_shop.controller;

import com.example.merch_shop.dto.ErrorResponse;
import com.example.merch_shop.dto.SendCoinRequest;
import com.example.merch_shop.dto.SendCoinResponse;
import com.example.merch_shop.model.CoinTransfer;
import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.CoinTransferRepository;
import com.example.merch_shop.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api")
public class SendCoinController {
    private final UserRepository userRepository;
    private final CoinTransferRepository coinTransferRepository;

    public SendCoinController(UserRepository userRepository, CoinTransferRepository coinTransferRepository) {
        this.userRepository = userRepository;
        this.coinTransferRepository = coinTransferRepository;
    }


    @PostMapping("/sendCoin")
    public ResponseEntity<?> sendCoin(@RequestBody SendCoinRequest request, Principal principal) {
        try {
            User sender = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));
            if (request.amount() > sender.getCoins()) {
                return ResponseEntity.badRequest().body(
                        ErrorResponse.builder()
                                .errors("Insufficient coins balance")
                                .build()
                );
            }

            log.info("Sending coins from {} to {}", principal.getName(), request.toUser());

            User receiver = userRepository.findByUsername(request.toUser())
                    .orElseThrow(() -> new UsernameNotFoundException("Receiver  not found: " + request.toUser()));

            sender.setCoins(sender.getCoins() - request.amount());
            receiver.setCoins(receiver.getCoins() + request.amount());
            userRepository.save(sender);
            userRepository.save(receiver);

            coinTransferRepository.save(CoinTransfer.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .amount(request.amount())
                    .createdAt(LocalDateTime.now())
                    .build());

            return ResponseEntity.ok(
                    new SendCoinResponse("Coins transferred successfully")
            );
        } catch (UsernameNotFoundException ex) {
            log.error("Transfer error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .errors(ex.getMessage()).build());
        } catch (Exception ex) {
            log.error("Internal error: {}", ex.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.builder()
                            .errors("Internal server error").build());
        }
    }
}
