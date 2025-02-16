package com.example.merch_shop.controller;

import com.example.merch_shop.dto.ErrorResponse;
import com.example.merch_shop.dto.InfoResponse;
import com.example.merch_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<?> getInfo(Principal principal) {
        try {
            String username = principal.getName();
            InfoResponse response = userService.getUserInfo(username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .errors(ex.getMessage())
                            .build()
            );
        }
    }
}