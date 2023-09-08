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
import com.hms.dto.DepartmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.Department;
import com.hms.models.User;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(value = "admin-service")
public interface AdminServiceProxy {

	@PostMapping(value = "/register")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "adminRegisterFallback")
	public ResponseEntity<JwtResponse> adminRegister(@RequestBody User admin) throws Exception;

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

	@PostMapping(value = "/doctors")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "registerDoctorFallback")
	public ResponseEntity<JwtResponse> registerDoctor(@RequestBody User doctor, @RequestHeader("Authorization") String token);

	@GetMapping(value = "/doctors")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAllDoctorsFallback")
	public ResponseEntity<JwtResponse> getAllDoctors(@RequestHeader("Authorization") String token);

	@GetMapping(value = "/doctors/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getDoctorByIdFallback")
	public ResponseEntity<JwtResponse> getDoctorById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

	@PutMapping(value = "/doctors")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "updateDoctorFallback")
	public ResponseEntity<JwtResponse> updateDoctor(@RequestBody User doctor, @RequestHeader("Authorization") String token);

	@DeleteMapping(value = "/doctors/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "deleteDoctorFallback")
	public ResponseEntity<JwtResponse> deleteDoctor(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

	@GetMapping(value = "/patients")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAllPatientsFallback")
	public ResponseEntity<JwtResponse> getAllPatients(@RequestHeader("Authorization") String token);

	@GetMapping(value = "/patients/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getPatientByIdFallback")
	public ResponseEntity<JwtResponse> getPatientById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

	@PutMapping(value = "/patients")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "updatePatientFallback")
	public ResponseEntity<JwtResponse> updatePatient(@RequestBody User patient, @RequestHeader("Authorization") String token);

	@DeleteMapping(value = "/patients/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "deletePatientFallback")
	public ResponseEntity<JwtResponse> deletePatient(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

	@GetMapping(value = "/appointments")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAllAppointmentsFallback")
	public ResponseEntity<AppointmentResponse> getAllAppointments(@RequestHeader("Authorization") String token);
	
	@GetMapping(value = "/appointments/{id}")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "getAppointmentByIdFallback")
	public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
	
	@PostMapping(value = "/appointments")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "createAppointmentFallback")
	public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody Appointment appointment, @RequestHeader("Authorization") String token);
	
	@PutMapping(value = "/appointments")
	@Retry(name = "admin-service")
	@CircuitBreaker(name = "admin-service", fallbackMethod = "updateAppointmentFallback")
	public ResponseEntity<AppointmentResponse> updateAppointment(@RequestBody Appointment appointment, @RequestHeader("Authorization") String token);

	// Fallback methods
	public default ResponseEntity<JwtResponse> adminRegisterFallback(User admin, Exception ex) {
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

	public default ResponseEntity<JwtResponse> registerDoctorFallback(Exception ex) {
		System.out.println("exception in register doctor fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> getAllDoctorsFallback(Exception ex) {
		System.out.println("exception in get all doctors fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> getDoctorByIdFallback(Exception ex) {
		System.out.println("exception in get doctor by id fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> updateDoctorFallback(Exception ex) {
		System.out.println("exception in update doctor fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> deleteDoctorFallback(Exception ex) {
		System.out.println("exception in delete doctor fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> getAllPatientsFallback(Exception ex) {
		System.out.println("exception in get all patients fallback: "+ex);
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

	public default ResponseEntity<JwtResponse> updatePatientFallback(Exception ex) {
		System.out.println("exception in update patient fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<JwtResponse> deletePatientFallback(Exception ex) {
		System.out.println("exception in delete patient fallback: "+ex);
		JwtResponse myResponse = new JwtResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

	public default ResponseEntity<AppointmentResponse> getAllAppointmentsFallback(Exception ex) {
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

	public default ResponseEntity<AppointmentResponse> updateAppointmentFallback(Exception ex) {
		System.out.println("exception in update appointment fallback: "+ex);
		AppointmentResponse myResponse = new AppointmentResponse();
		myResponse.setSuccess(false);
		myResponse.setError(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	}

}
