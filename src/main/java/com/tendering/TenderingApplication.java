package com.tendering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TenderingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenderingApplication.class, args);
	}
}
