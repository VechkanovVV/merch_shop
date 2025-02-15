package com.example.merch_shop;

import com.example.merch_shop.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
public class MerchShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchShopApplication.class, args);
    }

}
