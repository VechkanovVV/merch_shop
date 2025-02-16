package com.example.merch_shop.repository;

import com.example.merch_shop.model.InventoryProjection;
import com.example.merch_shop.model.Purchase;
import com.example.merch_shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @Query("SELECT p.product.name as type, COUNT(p) as quantity FROM Purchase p WHERE p.user = :user GROUP BY p.product.name")
    List<InventoryProjection> getInventory(@Param("user") User user);
}
