package com.ebuy.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class EbuyCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbuyCartServiceApplication.class, args);
	}

}
