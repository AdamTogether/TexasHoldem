package com.algorandex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;

@SpringBootApplication
public class TexasHoldemApplication {
	
//	@CacheEvict
	public static void main(String[] args) {
		SpringApplication.run(TexasHoldemApplication.class, args);
	}

}
