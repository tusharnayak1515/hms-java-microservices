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

import com.hms.dto.DepartmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Department;
import com.hms.models.User;
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
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(myResponse);
	        } 
			catch (BadCredentialsException e) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Invalid username or password");
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(myResponse);
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
			if (u1 != null && user.getUserId() != u1.getUserId()) {
				JwtResponse myresponse = new JwtResponse();
				myresponse.setSuccess(false);
				myresponse.setError("This mobile is already taken");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myresponse);
			}
			
			user.setUsername(request.getUsername());
			user.setMobile(request.getMobile());
			user.setAddress(request.getAddress());
			
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
			JwtResponse myResponse = new JwtResponse();
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
			JwtResponse myResponse = new JwtResponse();
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
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
	
//	@GetMapping("/department/{name}")
//	public ResponseEntity<?> getDepartmentsByName(@PathVariable String name) throws Exception {
//		try {
//			Department department = departmentService.findByName(name);
//			DepartmentResponse myResponse = new DepartmentResponse();
//			myResponse.setSuccess(true);
//			myResponse.setDepartment(department);
//			log.debug("In admin-service get department by name with department data: "+department);
//			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
//		} catch (Exception e) {
//			JwtResponse myResponse = new JwtResponse();
//			myResponse.setSuccess(false);
//			myResponse.setError(e.toString());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
//		}
//	}
	
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
			
			department = departmentService.updateDepartment(department);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			myResponse.setDepartment(department);
			log.debug("In admin-service update department with department data: "+department);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
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
			
			departmentService.deleteDepartment(id);
			DepartmentResponse myResponse = new DepartmentResponse();
			myResponse.setSuccess(true);
			log.debug("In admin-service delete department with res: "+true);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);	
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
}
