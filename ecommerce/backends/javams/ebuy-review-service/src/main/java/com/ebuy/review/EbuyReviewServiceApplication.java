package com.ebuy.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.ebuy.review")
public class EbuyReviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbuyReviewServiceApplication.class, args);
	}

}
