package com.hms.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope(value = "request")
public class AdminController {
	
	@GetMapping("/register")
	public String test() {
		return "Hello";
	}
}
