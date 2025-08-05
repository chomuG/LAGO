package com.example.LAGO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.LAGO.repository")
public class LagoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LagoApplication.class, args);
	}

}
