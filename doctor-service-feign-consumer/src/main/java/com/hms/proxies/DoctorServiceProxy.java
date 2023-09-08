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

import com.hms.dto.AppointmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.User;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(value = "doctor-service")
public interface DoctorServiceProxy {

	@PostMapping(value = "/register")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "doctorRegisterFallback")
	public ResponseEntity<JwtResponse> doctorRegister(@RequestBody User doctor) throws Exception;

	@PostMapping(value = "/login")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "doctorLoginFallback")
	public ResponseEntity<JwtResponse> doctorLogin(@RequestBody LoginRequest request) ;

	@PutMapping(value = "/update-profile")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "updateProfileFallback")
	public ResponseEntity<JwtResponse> updateProfile(@RequestBody User request, @RequestHeader("Authorization") String token) ;
	
	@GetMapping(value = "/patients")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "getMyPatientsFallback")
	public ResponseEntity<JwtResponse> getMyPatients(@RequestHeader("Authorization") String token);
	
	@GetMapping(value = "/patients/discharged")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "getMyDischargedPatients")
	public ResponseEntity<JwtResponse> getMyDischargedPatients(@RequestHeader("Authorization") String token);

	@GetMapping(value = "/patients/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getPatientByIdFallback")
	public ResponseEntity<JwtResponse> getPatientById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

	@GetMapping(value = "/appointments")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "getMyAppointmentsFallback")
	public ResponseEntity<AppointmentResponse> getMyAppointments(@RequestHeader("Authorization") String token);
	
	@GetMapping(value = "/appointments/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAppointmentByIdFallback")
	public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
	
	@PutMapping(value = "/appointments")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "updateAppointmentFallback")
	public ResponseEntity<AppointmentResponse> updateAppointment(@RequestBody Appointment appointment, @RequestHeader("Authorization") String token);
	
	@DeleteMapping(value = "/appointments/{id}")
	@Retry(name = "doctor-service")
	@CircuitBreaker(name = "doctor-service", fallbackMethod = "deleteAppointmentFallback")
	public ResponseEntity<AppointmentResponse> deleteAppointment(@PathVariable Long id, @RequestHeader("Authorization") String token);

	// Fallback methods
	public default ResponseEntity<JwtResponse> doctorRegisterFallback(User admin, Exception ex) {
		System.out.println("admin: "+admin.getMobile());
		System.out.println("--------- error message: "+ex.toString());
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> doctorLoginFallback(Exception ex) {
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

	public default ResponseEntity<JwtResponse> getMyPatientsFallback(Exception ex) {
		System.out.println("exception in get my patients fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> getMyDischargedPatients(Exception ex) {
		System.out.println("exception in get my discharged patients fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> getPatientByIdFallback(Exception ex) {
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

	public default ResponseEntity<AppointmentResponse> updateAppointmentFallback(Exception ex) {
		System.out.println("exception in update appointment fallback: "+ex);
		AppointmentResponse myResponse = new AppointmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<AppointmentResponse> deleteAppointmentFallback(Exception ex) {
		System.out.println("exception in delete appointment fallback: "+ex);
		AppointmentResponse myResponse = new AppointmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

}
