package com.example.LAGO;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching	// 스프링 캐시 활성화
public class LagoApplication {

	public static void main(String[] args) {

		SpringApplication.run(LagoApplication.class, args);
	}

}
