package com.hms.controllers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Department;
import com.hms.models.User;
import com.hms.proxies.AdminServiceProxy;

import feign.FeignException;

@RestController
@Scope(value = "request")
@RequestMapping("/api")
public class AdminConsumerRestController {

	@Autowired
	private AdminServiceProxy adminServiceProxy;

	private Logger log = LoggerFactory.getLogger(AdminConsumerRestController.class);

	@PostMapping("/register")
	public ResponseEntity<?> adminRegister(@RequestBody User admin) throws Exception {
		try {
			log.debug("In admin register with data: " + admin);
			return adminServiceProxy.adminRegister(admin);
		} catch (FeignException e) {
			log.debug("Error: In admin register: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse response) throws Exception {
		try {
			log.debug("In admin login with data: " + request.getMobile());
			JwtResponse response1 = (JwtResponse)
			adminServiceProxy.adminLogin(request).getBody();
			Cookie jwtCookie = new Cookie("authorization", response1.getToken());
			System.out.println("jwtCookie: "+jwtCookie.getValue());
			jwtCookie.setMaxAge(86400000);
			jwtCookie.setPath("/");
			response.addCookie(jwtCookie);

			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(true);
			myResponse.setUser(response1.getUser());
			myResponse.setToken(jwtCookie.getValue());
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			// ResponseEntity<?> myResponse = adminServiceProxy.adminLogin(request);
			// JwtResponse response1 = (JwtResponse) myResponse.getBody();
			// System.out.println("response1 token: "+response1.getToken());
			// return myResponse;
		} catch (Exception e) {
			log.debug("Error: In admin login: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PutMapping("/update-profile")
	public ResponseEntity<?> updateProfile(@RequestBody User request) throws Exception {
		try {
			log.debug("In update profile with data: " + request);
			return adminServiceProxy.updateProfile(request);
		} catch (Exception e) {
			log.debug("Error: In update profile: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PostMapping("/department")
	public ResponseEntity<?> createDepartment(@RequestBody Department department, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In create department with data: " + department);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						// cookie.setValue(null);
						// cookie.setMaxAge(0);
						// cookie.setPath("/");
						// response.addCookie(cookie);
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.createDepartment(department,token);
		} catch (Exception e) {
			log.debug("Error: In create department: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/department")
	public ResponseEntity<?> getAllDepartments(HttpServletRequest httpRequest, HttpServletResponse response)
			throws Exception {
		try {
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						// cookie.setValue(null);
						// cookie.setMaxAge(0);
						// cookie.setPath("/");
						// response.addCookie(cookie);
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.getAllDepartments(token);
		} catch (Exception e) {
			log.debug("Error: In get all departmments: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/department/{id}")
	public ResponseEntity<?> getDepartmentById(@PathVariable Long id, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get department by id with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						// cookie.setValue(null);
						// cookie.setMaxAge(0);
						// cookie.setPath("/");
						// response.addCookie(cookie);
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.getDepartmentById(id,token);
		} catch (Exception e) {
			log.debug("Error: In get department by id: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	// @GetMapping("/department/{name}")
	// public ResponseEntity<?> getDepartmentsByName(@PathVariable String name) throws Exception {
	// 	try {
	// 		log.debug("In get department by name with name: " + name);
	// 		return adminServiceProxy.getDepartmentsByName(name);
	// 	} catch (Exception e) {
	// 		log.debug("Error: In get department by name: " + e.toString());
	// 		JwtResponse myResponse = new JwtResponse();
	// 		myResponse.setSuccess(false);
	// 		myResponse.setError(e.toString());
	// 		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
	// 	}
	// }

	@PutMapping("/department")
	public ResponseEntity<?> updateDepartment(@RequestBody Department department, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In update department with department data: " + department);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						// cookie.setValue(null);
						// cookie.setMaxAge(0);
						// cookie.setPath("/");
						// response.addCookie(cookie);
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.updateDepartment(department,token);
		} catch (Exception e) {
			log.debug("Error: In update department: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@DeleteMapping("/department/{id}")
	public ResponseEntity<?> deleteDepartment(@PathVariable long id, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In delete department with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						// cookie.setValue(null);
						// cookie.setMaxAge(0);
						// cookie.setPath("/");
						// response.addCookie(cookie);
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.deleteDepartment(id,token);
		} catch (Exception e) {
			log.debug("Error: In delete department: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	// private String getJwtTokenFromRequest(HttpServletRequest request) {
	// 	Cookie[] cookies = request.getCookies();
	// 	if (cookies != null) {
	// 		for (Cookie cookie : cookies) {
	// 			System.out.println(cookie.getName()+": "+cookie.getValue());
	// 			// cookie.setValue(null);
	// 			// cookie.setMaxAge(0);
	// 			// cookie.setPath("/");
	// 			if ("authorization".equalsIgnoreCase(cookie.getName())) {
	// 				return cookie.getValue();
	// 			}
	// 		}
	// 	}
	// 	return null;
	// }
}
