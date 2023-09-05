package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class HmsConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HmsConfigServerApplication.class, args);
	}

}
