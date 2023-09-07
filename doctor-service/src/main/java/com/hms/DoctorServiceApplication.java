package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import brave.sampler.Sampler;

@SpringBootApplication
public class DoctorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoctorServiceApplication.class, args);
	}
	
	@Bean
	public Sampler getSample() {
		return Sampler.ALWAYS_SAMPLE;
	}

}
