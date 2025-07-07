package com.mcm.backend.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
	public static void main(String[] args) {

		// Initialize
		InitializeUtil.initialize();

		// Run Spring Boot
		SpringApplication.run(BackendApplication.class, args);
	}
}
