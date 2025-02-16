package com.example.merch_shop.controller;

import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.UserRepository;
import com.example.merch_shop.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        shopService.purchaseItem(user, item);
        return ResponseEntity.ok().build();
    }
}
