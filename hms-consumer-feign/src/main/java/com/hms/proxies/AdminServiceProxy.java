package com.hms.proxies;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.hms.dto.DepartmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Department;
import com.hms.models.User;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient("admin-service")
public interface AdminServiceProxy {

	@PostMapping(value = "/register")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "adminRegisterFallback")
	public ResponseEntity<?> adminRegister(@RequestBody User admin) throws Exception;

	@PostMapping(value = "/login")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "adminLoginFallback")
	public ResponseEntity<JwtResponse> adminLogin(@RequestBody LoginRequest request) ;

	@PutMapping(value = "/update-profile")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "updateProfileFallback")
	public ResponseEntity<?> updateProfile(@RequestBody User request) ;

	@PostMapping(value = "/department")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "createDepartmentFallback")
	public ResponseEntity<?> createDepartment(@RequestBody Department department, @RequestHeader("Authorization") String token) ;

	@GetMapping(value = "/department")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAllDepartmentsFallback")
	public ResponseEntity<?> getAllDepartments(@RequestHeader("Authorization") String token);

	@GetMapping(value = "/department/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getDepartmentByIdFallback")
	public ResponseEntity<?> getDepartmentById(@PathVariable Long id, @RequestHeader("Authorization") String token) ;

	// @GetMapping(value = "/department/{name}")
	// @Retry(name = "admin-service")
	// @CircuitBreaker(name = "admin-service", fallbackMethod = "getDepartmentsByNameFallback")
	// public ResponseEntity<?> getDepartmentsByName(@PathVariable String name);

	@PutMapping(value = "/department")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "updateDepartmentFallback")
	public ResponseEntity<?> updateDepartment(@RequestBody Department department, @RequestHeader("Authorization") String token) ;

	@DeleteMapping(value = "/department/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "deleteDepartmentFallback")
	public ResponseEntity<?> deleteDepartment(@PathVariable Long id, @RequestHeader("Authorization") String token) ;

	// Fallback methods
	public default ResponseEntity<?> adminRegisterFallback(User admin, Exception ex) {
		System.out.println("admin: "+admin.getMobile());
		System.out.println("--------- error message: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Registration failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> adminLoginFallback(Throwable cause) {
		System.out.println("yes----------");
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Login failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> updateProfileFallback(Throwable cause) {
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Update profile failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> createDepartmentFallback(Throwable cause) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Create department failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> getAllDepartmentsFallback(Throwable cause) {
		// System.out.println("httpRequest: "+httpRequest);
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Get all departments failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> getDepartmentByIdFallback(Throwable cause) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Get department by id failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> getDepartmentsByNameFallback(Throwable cause) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Get department by name failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> updateDepartmentFallback(Throwable cause) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Update department failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> deleteDepartmentFallback(Throwable cause) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Delete department failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

}
