package com.example.merch_shop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int coins = 1000;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "authorities",
            joinColumns = @JoinColumn(name = "username", referencedColumnName = "username")
    )
    @Column(name = "authority")
    private List<String> authorities = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.authorities == null || this.authorities.isEmpty()) {
            log.info("Setting default authorities for user: {}", username);
            this.authorities = List.of("USER");
        }
    }
}