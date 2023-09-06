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

import com.hms.config.FeignClientConfig;
import com.hms.dto.DepartmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Department;
import com.hms.models.User;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(value = "admin-service", configuration = FeignClientConfig.class)
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
	public ResponseEntity<JwtResponse> updateProfile(@RequestBody User request, @RequestHeader("Authorization") String token) ;

	@PostMapping(value = "/department")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "createDepartmentFallback")
	public ResponseEntity<DepartmentResponse> createDepartment(@RequestBody Department department, @RequestHeader("Authorization") String token) ;

	@GetMapping(value = "/department")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAllDepartmentsFallback")
	public ResponseEntity<DepartmentResponse> getAllDepartments(@RequestHeader("Authorization") String token);

	@GetMapping(value = "/department/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getDepartmentByIdFallback")
	public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id, @RequestHeader("Authorization") String token) ;

	@GetMapping(value = "/department/name/{name}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getDepartmentsByNameFallback")
	public ResponseEntity<DepartmentResponse> getDepartmentsByName(@PathVariable String name, @RequestHeader("Authorization") String token);

	@PutMapping(value = "/department")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "updateDepartmentFallback")
	public ResponseEntity<DepartmentResponse> updateDepartment(@RequestBody Department department, @RequestHeader("Authorization") String token) ;

	@DeleteMapping(value = "/department/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "deleteDepartmentFallback")
	public ResponseEntity<DepartmentResponse> deleteDepartment(@PathVariable Long id, @RequestHeader("Authorization") String token) ;

	// Fallback methods
	public default ResponseEntity<?> adminRegisterFallback(User admin, Exception ex) {
		System.out.println("admin: "+admin.getMobile());
		System.out.println("--------- error message: "+ex.toString());
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> adminLoginFallback(Exception ex) {
		System.out.println("yes----------"+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<?> updateProfileFallback(Exception ex) {
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Update profile failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<DepartmentResponse> createDepartmentFallback(Exception ex) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<DepartmentResponse> getAllDepartmentsFallback(Exception ex) {
		// System.out.println("httpRequest: "+httpRequest);
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Get all departments failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<DepartmentResponse> getDepartmentByIdFallback(Exception ex) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Get department by id failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<DepartmentResponse> getDepartmentsByNameFallback(Exception ex) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError("Get department by name failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<DepartmentResponse> updateDepartmentFallback(Exception ex) {
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<DepartmentResponse> deleteDepartmentFallback(Exception ex) {
		System.out.println("execption in delete department fallback: "+ex);
		DepartmentResponse myResponse = new DepartmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

}
