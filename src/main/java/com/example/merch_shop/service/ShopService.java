package com.example.merch_shop.service;

import lombok.extern.slf4j.Slf4j;
import com.example.merch_shop.exception.InsufficientCoinsException;
import com.example.merch_shop.exception.ProductNotFoundException;
import com.example.merch_shop.model.Product;
import com.example.merch_shop.model.Purchase;
import com.example.merch_shop.model.User;
import com.example.merch_shop.repository.ProductRepository;
import com.example.merch_shop.repository.PurchaseRepository;
import com.example.merch_shop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShopService {
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;


    public ShopService(ProductRepository productRepository, PurchaseRepository purchaseRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
    }

    public void purchaseItem(User user, String itemName) {
        log.debug("Purchasing item for user: {}", user.getUsername());
        log.debug("User password present: {}", user.getPassword() != null);
        Product product = productRepository.findById(itemName)
                .orElseThrow(() -> new ProductNotFoundException("Item not found"));

        if (user.getCoins() < product.getPrice()) {
            throw new InsufficientCoinsException("Not enough coins");
        }
        user.setCoins(user.getCoins() - product.getPrice());
        userRepository.saveAndFlush(user);

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setProduct(product);
        purchaseRepository.save(purchase);
    }
}
