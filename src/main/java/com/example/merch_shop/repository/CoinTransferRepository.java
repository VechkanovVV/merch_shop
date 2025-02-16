package com.example.merch_shop.repository;

import com.example.merch_shop.model.CoinTransfer;
import com.example.merch_shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinTransferRepository extends JpaRepository<CoinTransfer, Long> {
    List<CoinTransfer> findBySenderOrReceiver(User sender, User receiver);
}
