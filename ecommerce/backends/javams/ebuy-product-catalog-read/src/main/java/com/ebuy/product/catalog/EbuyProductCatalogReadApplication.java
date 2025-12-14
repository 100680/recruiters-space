package com.ebuy.product.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class EbuyProductCatalogReadApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbuyProductCatalogReadApplication.class, args);
	}

}
