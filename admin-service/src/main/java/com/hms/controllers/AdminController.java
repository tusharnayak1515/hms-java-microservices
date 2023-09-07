package com.hms.controllers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hms.dto.AppointmentResponse;
import com.hms.dto.DepartmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.Department;
import com.hms.models.User;
import com.hms.services.AppointmentService;
import com.hms.services.CustomUserDetailsService;
import com.hms.services.DepartmentService;
import com.hms.utils.JwtUtil;

@RestController
@Scope(value = "request")
public class AdminController {
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private AppointmentService appointmentService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
    private JwtUtil jwtUtil;
	
	private Logger log = LoggerFactory.getLogger(AdminController.class);
	
	@PostMapping(value = "/register", produces = "application/json")
	public ResponseEntity<?> adminRegister(@RequestBody User admin) throws Exception {
		try {
			log.debug("In admin-service admin register with data: "+admin);
			User isUser1 = customUserDetailsService.findByUsername(admin.getUsername());
			User isUser2 = customUserDetailsService.findByMobile(admin.getMobile());
			
			if(isUser1 != null && isUser2 != null) {
//				throw new Exception("User already exists");
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("User already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			admin.setRole("admin");
			admin.setStatus("approved");
			admin.setPassword(passwordEncoder.encode(admin.getPassword()));
			admin = customUserDetailsService.createUser(admin);
			log.debug("In admin-service admin register with admin data: "+admin);
			JwtResponse myResponse = new JwtResponse();
            myResponse.setSuccess(true);
            return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
//			throw e;
			JwtResponse myResponse = new JwtResponse();
            myResponse.setSuccess(false);
            myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
		
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request, HttpServletResponse response) throws Exception {
		try {
			log.debug("In admin-service admin login with mobile: "+request.getMobile());
			String mobileRegex = "^[0-9]{10}$";
	        Pattern mobilePattern = Pattern.compile(mobileRegex);
	        Matcher mobileMatcher = mobilePattern.matcher(request.getMobile());
	        
	        if (!mobileMatcher.matches()) {
	            JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Invalid mobile");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	        }
	        
	        if (request.getPassword().replaceAll("\\s+", "").length() == 0) {
	            JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Password cannot be empty");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	        }
	        
	        try {
	        	authenticationManager.authenticate(
	        			new UsernamePasswordAuthenticationToken(request.getMobile(), request.getPassword()));				
			} 
	        catch (DisabledException e) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("User account is disabled");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
	        } 
			catch (BadCredentialsException e) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Invalid username or password");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
	        
	        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getMobile());
	        String token = jwtUtil.generateToken(userDetails);
	        Cookie jwtCookie = new Cookie("authorization", token);
	        jwtCookie.setMaxAge(86400000);
	        jwtCookie.setPath("/");
	        response.addCookie(jwtCookie);
	        
	        User user = customUserDetailsService.findByMobile(request.getMobile());
	        user.setPassword(null);
	        
	        JwtResponse myResponse = new JwtResponse();
	        myResponse.setSuccess(true);
	        myResponse.setUser(user);
	        myResponse.setToken(token);
	        log.debug("In admin-service admin login with admin data: "+user);
	        return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} 
		catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
		
	}
	
	@PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody User request, HttpServletRequest request1,
            HttpServletResponse response) throws Exception {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if (request.getUsername().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Username cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if (request.getMobile().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Mobile cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			String mobileRegex = "^[0-9]{10}$";
			Pattern mobilePattern = Pattern.compile(mobileRegex);
			Matcher mobileMatcher = mobilePattern.matcher(request.getMobile());
			
			if (!mobileMatcher.matches()) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Invalid mobile");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if (request.getAddress().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Address cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			User u1 = this.customUserDetailsService.findByMobile(request.getMobile());
			User u2 = this.customUserDetailsService.findByUsername(request.getUsername());
			if (u1 != null && u2 != null && user.getUserId() != u1.getUserId() && user.getUserId() != u2.getUserId()) {
				JwtResponse myresponse = new JwtResponse();
				myresponse.setSuccess(false);
				myresponse.setError("This mobile is already taken");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myresponse);
			}
			
			user.setUsername(request.getUsername());
			user.setMobile(request.getMobile());
			user.setAddress(request.getAddress());
			user.setDp(request.getDp());
			
			user = customUserDetailsService.update(user);
			
			Cookie[] jwtCookies = request1.getCookies();
			if (jwtCookies != null) {
				for (Cookie cookie : jwtCookies) {
					if (cookie.getName().equals("authorization")) {
						cookie.setValue(null);
						cookie.setMaxAge(0);
						cookie.setPath("/");
						response.addCookie(cookie);
					}
				}
			}
			
			final UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getMobile());
			final String token = jwtUtil.generateToken(userDetails);
			
			Cookie jwtCookie = new Cookie("authorization", token);
			jwtCookie.setMaxAge(86400000);
			jwtCookie.setPath("/");
			response.addCookie(jwtCookie);
			
			user.setPassword(null);
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			myResponse.setUser(user);
			myResponse.setToken(token);
			log.debug("In admin-service update profile with user data: "+user);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
    }
	
	@PostMapping("/department")
	public ResponseEntity<?> createDepartment(@RequestBody Department department) throws Exception {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Not Allowed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if(department.getDepartmentName() == null || (department.getDepartmentName() != null && department.getDepartmentName().replaceAll("\\s+", "").length() == 0)) { 
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Department name cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			Department isDepartent = departmentService.findByName(department.getDepartmentName());
			if(isDepartent != null) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Department exists with the same name");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			department = departmentService.createDepartment(department);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			myResponse.setDepartment(department);
			log.debug("In admin-service create department with department data: "+department);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/department")
	public ResponseEntity<?> getAllDepartments(HttpServletRequest request) throws Exception {
		try {
			Cookie[] jwtCookies = request.getCookies();
	        String token = null;
	        System.out.println("cookies: "+jwtCookies);
	        if (jwtCookies != null) {
	            for (Cookie cookie : jwtCookies) {
	            	log.debug("Cookie name: "+cookie.getName());
	                if (cookie.getName().equalsIgnoreCase("authorization")) {
	                    token = cookie.getValue();
	                }
	            }
	        }
	        log.debug("JWT Token: "+token);
			List<Department> departments = departmentService.findAll();
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			myResponse.setDepartments(departments);
			log.debug("In admin-service get all departmments with departments data: "+departments);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/department/{id}")
	public ResponseEntity<?> getDepartmentById(@PathVariable Long id) throws Exception {
		try {
			Department department = departmentService.findById(id);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			myResponse.setDepartment(department);
			log.debug("In admin-service get department by id with department data: "+department);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/department/name/{name}")
	public ResponseEntity<?> getDepartmentsByName(@PathVariable String name) throws Exception {
		try {
			Department department = departmentService.findByName(name);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			myResponse.setDepartment(department);
			log.debug("In admin-service get department by name with department data: "+department);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@PutMapping("/department")
	public ResponseEntity<?> updateDepartment(@RequestBody Department department) throws Exception {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Not Allowed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			Department isDepartment = departmentService.findById(department.getDepartmentId());
			if(isDepartment == null) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Department not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			System.out.println("department.getDepartmentName(): "+department.getDepartmentName());
						
			if(department.getDepartmentName() == null || (department.getDepartmentName() != null && department.getDepartmentName().replaceAll("\\s+", "").length() == 0)) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Department name cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			isDepartment = departmentService.findByName(department.getDepartmentName());
			if(isDepartment != null) {
				if(isDepartment.getDepartmentId() != department.getDepartmentId()) {
					DepartmentResponse myResponse = new DepartmentResponse();
					myResponse.setSuccess(false);
					myResponse.setError("Department exists with the same name");
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);					
				}
			}
			
			department = departmentService.updateDepartment(department);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			myResponse.setDepartment(department);
			log.debug("In admin-service update department with department data: "+department);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@DeleteMapping("/department/{id}")
	public ResponseEntity<?> deleteDepartment(@PathVariable Long id) throws Exception {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Not Allowed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			Department department = departmentService.findById(id);
			if(department == null) {
				DepartmentResponse myResponse = new DepartmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Department not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			departmentService.deleteDepartment(id);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			log.debug("In admin-service delete department with res: "+true);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);	
		} catch (Exception e) {
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	
	//	Doctors Section
	
	@PostMapping("/doctors")
	public ResponseEntity<?> registerDoctor(@RequestBody User doctor) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			log.debug("In admin-service doctor register with data: "+doctor);
			User isUser1 = customUserDetailsService.findByUsername(doctor.getUsername());
			User isUser2 = customUserDetailsService.findByMobile(doctor.getMobile());
			
			if(isUser1 != null && isUser2 != null) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("User already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			doctor.setRole("doctor");
			doctor.setStatus("approved");
			doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
			doctor = customUserDetailsService.createUser(doctor);
			log.debug("In admin-service doctor register with doctor data: "+doctor);
			JwtResponse myResponse = new JwtResponse();
            myResponse.setSuccess(true);
            return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/doctors")
	public ResponseEntity<?> getAllDoctors() {
		try {
			List<User> doctors = this.customUserDetailsService.findByRole("doctor");
			Iterator<User> iterator = doctors.iterator();
			while (iterator.hasNext()) {
                User element = iterator.next();
                element.setPassword(null);
            }
			JwtResponse myResponse = new JwtResponse();
            myResponse.setSuccess(true);
            myResponse.setUsers(doctors);
            return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/doctors/{id}")
	public ResponseEntity<?> getDoctorById(@PathVariable("id") Long id) {
		try {
			User doctor = this.customUserDetailsService.findById(id);
			if(doctor != null) {
				doctor.setPassword(null);				
			}
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			myResponse.setUser(doctor);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@PutMapping("/doctors")
	public ResponseEntity<?> updateDoctor(@RequestBody User doctor) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			User isDoctor = this.customUserDetailsService.findById(doctor.getUserId());
			if(isDoctor == null) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("User not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			if (doctor.getUsername().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Username cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if (doctor.getMobile().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Mobile cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			String mobileRegex = "^[0-9]{10}$";
			Pattern mobilePattern = Pattern.compile(mobileRegex);
			Matcher mobileMatcher = mobilePattern.matcher(doctor.getMobile());
			
			if (!mobileMatcher.matches()) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Invalid mobile");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if (doctor.getAddress().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Address cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			isDoctor = this.customUserDetailsService.findByMobile(doctor.getMobile());
			User isDoctor2 = this.customUserDetailsService.findByUsername(doctor.getUsername());
			if (isDoctor != null && isDoctor2 != null && doctor.getUserId() != isDoctor.getUserId() && doctor.getUserId() != isDoctor2.getUserId()) {
				JwtResponse myresponse = new JwtResponse();
				myresponse.setSuccess(false);
				myresponse.setError("This mobile is already taken");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myresponse);
			}
			
			Department department = this.departmentService.findById(doctor.getDepartment().getDepartmentId());
			if(department == null) {
				JwtResponse myresponse = new JwtResponse();
				myresponse.setSuccess(false);
				myresponse.setError("Department not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myresponse);
			}
			
			doctor = this.customUserDetailsService.update(doctor);
			doctor.setPassword(null);
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			myResponse.setUser(doctor);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@DeleteMapping("/doctors/{id}")
	public ResponseEntity<?> deleteDoctor(@PathVariable("id") Long id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			User doctor = this.customUserDetailsService.findById(id);
			if(doctor == null) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Doctor not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			this.customUserDetailsService.delete(id);
			
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	
	//	Patients section
	
	@GetMapping("/patients")
	public ResponseEntity<?> getAllPatients() {
		try {
			List<User> patients = this.customUserDetailsService.findByRole("patient");
			Iterator<User> iterator = patients.iterator();
			while (iterator.hasNext()) {
                User element = iterator.next();
                element.setPassword(null);
            }
			JwtResponse myResponse = new JwtResponse();
            myResponse.setSuccess(true);
            myResponse.setUsers(patients);
            return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/patients/{id}")
	public ResponseEntity<?> getPatientById(@PathVariable("id") Long id) {
		try {
			User patient = this.customUserDetailsService.findById(id);
			if(patient != null) {
				patient.setPassword(null);				
			}
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			myResponse.setUser(patient);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@PutMapping("/patients")
	public ResponseEntity<?> updatePatient(@RequestBody User patient) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			User isPatient = this.customUserDetailsService.findById(patient.getUserId());
			if(isPatient == null) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Patient not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			if (patient.getUsername().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Username cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if (patient.getMobile().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Mobile cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			String mobileRegex = "^[0-9]{10}$";
			Pattern mobilePattern = Pattern.compile(mobileRegex);
			Matcher mobileMatcher = mobilePattern.matcher(patient.getMobile());
			
			if (!mobileMatcher.matches()) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Invalid mobile");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if (patient.getAddress().replaceAll("\\s+", "").length() == 0) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Address cannot be empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			isPatient = this.customUserDetailsService.findByMobile(patient.getMobile());
			User isPatient2 = this.customUserDetailsService.findByUsername(patient.getUsername());
			if (isPatient != null && isPatient2 != null && patient.getUserId() != isPatient.getUserId() && patient.getUserId() != isPatient2.getUserId()) {
				JwtResponse myresponse = new JwtResponse();
				myresponse.setSuccess(false);
				myresponse.setError("This mobile is already taken");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myresponse);
			}
			
			patient = this.customUserDetailsService.update(patient);
			patient.setPassword(null);
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			myResponse.setUser(patient);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@DeleteMapping("/patients/{id}")
	public ResponseEntity<?> deletePatient(@PathVariable("id") Long id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			User patient = this.customUserDetailsService.findById(id);
			if(patient == null) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Patient not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			this.customUserDetailsService.delete(id);
			
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	
	// Appointment Section
	
	@GetMapping("/appointments")
	public ResponseEntity<?> getAllAppointments() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				AppointmentResponse myResponse = new AppointmentResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			List<Appointment> appointments = this.appointmentService.findAll();
			AppointmentResponse myResponse = new AppointmentResponse();
            myResponse.setSuccess(true);
            myResponse.setAppointments(appointments);
            return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@GetMapping("/appointments/{id}")
	public ResponseEntity<?> getAppointmentById(@PathVariable("id") Long id) {
		try {
			Appointment appointment = this.appointmentService.findById(id);
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(true);
			myResponse.setAppointment(appointment);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@PostMapping("/appointments")
	public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				AppointmentResponse myResponse = new AppointmentResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			appointment = this.appointmentService.createAppointment(appointment);
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(true);
			myResponse.setAppointment(appointment);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
	@PutMapping("/appointments")
	public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment) {
		try {
			System.out.println("appointment: "+appointment);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("admin")) {
				AppointmentResponse myResponse = new AppointmentResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			Appointment isAppointment = this.appointmentService.findById(appointment.getAppointmentId());
			if(isAppointment == null) {
				AppointmentResponse myResponse = new AppointmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Appointment not found");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			appointment = this.appointmentService.updateAppointment(appointment);
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(true);
			myResponse.setAppointment(appointment);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
}
