package com.hms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.proxies.AdminServiceProxy;

@RestController
@Scope(value = "request")
public class AdminConsumerRestController {
	
	@Autowired
	private AdminServiceProxy adminServiceProxy;
	
	@GetMapping("/register")
	public String test() {
		return adminServiceProxy.test();
	}
}
