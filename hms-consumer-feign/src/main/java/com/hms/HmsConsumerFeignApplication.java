package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class HmsConsumerFeignApplication {

	public static void main(String[] args) {
		SpringApplication.run(HmsConsumerFeignApplication.class, args);
	}

}
