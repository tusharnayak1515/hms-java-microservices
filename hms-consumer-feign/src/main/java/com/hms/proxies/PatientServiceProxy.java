package com.hms.proxies;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.hms.dto.AppointmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.User;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(value = "patient-service")
public interface PatientServiceProxy {

	@PostMapping(value = "/register")
	@Retry(name = "patient-service")
	@CircuitBreaker(name = "patient-service", fallbackMethod = "patientRegisterFallback")
	public ResponseEntity<JwtResponse> patientRegister(@RequestBody User doctor) throws Exception;

	@PostMapping(value = "/login")
	@Retry(name = "patient-service")
	@CircuitBreaker(name = "patient-service", fallbackMethod = "patientLoginFallback")
	public ResponseEntity<JwtResponse> patientLogin(@RequestBody LoginRequest request) ;

	@PutMapping(value = "/update-profile")
	@Retry(name = "patient-service")
	@CircuitBreaker(name = "patient-service", fallbackMethod = "updateProfileFallback")
	public ResponseEntity<JwtResponse> updateProfile(@RequestBody User request, @RequestHeader("Authorization") String token) ;

	@GetMapping(value = "/doctors/{id}")
	@Retry(name = "patient-service")
	@CircuitBreaker(name = "patient-service", fallbackMethod = "getDoctorByIdFallback")
	public ResponseEntity<JwtResponse> getDoctorById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

	@GetMapping(value = "/appointments")
	@Retry(name = "patient-service")
	@CircuitBreaker(name = "patient-service", fallbackMethod = "getMyAppointmentsFallback")
	public ResponseEntity<AppointmentResponse> getMyAppointments(@RequestHeader("Authorization") String token);
	
	@GetMapping(value = "/appointments/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAppointmentByIdFallback")
	public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
	
	@PostMapping(value = "/appointments")
	@Retry(name = "patient-service")
	@CircuitBreaker(name = "patient-service", fallbackMethod = "createAppointmentFallback")
	public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody Appointment appointment, @RequestHeader("Authorization") String token);

	// Fallback methods
	public default ResponseEntity<JwtResponse> patientRegisterFallback(User admin, Exception ex) {
		System.out.println("admin: "+admin.getMobile());
		System.out.println("--------- error message: "+ex.toString());
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> patientLoginFallback(Exception ex) {
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

	public default ResponseEntity<JwtResponse> getDoctorByIdFallback(Exception ex) {
		System.out.println("exception in get patient by id fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<AppointmentResponse> getMyAppointmentsFallback(Exception ex) {
		System.out.println("exception in get all appointments fallback: "+ex);
		AppointmentResponse myResponse = new AppointmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<AppointmentResponse> getAppointmentByIdFallback(Exception ex) {
		System.out.println("exception in get appointment by id fallback: "+ex);
		AppointmentResponse myResponse = new AppointmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<AppointmentResponse> createAppointmentFallback(Exception ex) {
		System.out.println("exception in create appointment fallback: "+ex);
		AppointmentResponse myResponse = new AppointmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

}
