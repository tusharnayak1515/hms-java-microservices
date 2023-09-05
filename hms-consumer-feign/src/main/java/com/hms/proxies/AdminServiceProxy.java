package com.hms.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("admin-service")
public interface AdminServiceProxy {
	
	@GetMapping(value = "/register")
	public String test();

}
