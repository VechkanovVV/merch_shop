package com.example.merch_shop.controller;

import com.example.merch_shop.exception.InsufficientCoinsException;
import com.example.merch_shop.exception.ProductNotFoundException;
import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.UserRepository;
import com.example.merch_shop.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class ShopController {
    private final ShopService shopService;
    private final UserRepository userRepository;

    public ShopController(ShopService shopService, UserRepository userRepository) {
        this.shopService = shopService;
        this.userRepository = userRepository;
    }

    @GetMapping("/buy/{item}")
    public ResponseEntity<?> buyItem(
            @PathVariable String item,
            Principal principal) {

        String username = principal.getName();

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            shopService.purchaseItem(user, item);
            return ResponseEntity.ok().body(
                    Map.of("message", "Purchase successful",
                            "remainingCoins", user.getCoins())
            );

        } catch (ProductNotFoundException ex) {
            log.error("Product not found: {}", item);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));

        } catch (InsufficientCoinsException ex) {
            log.error("Insufficient coins for user: {}", username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));

        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
