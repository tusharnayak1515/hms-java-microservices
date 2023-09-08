package com.hms.controllers;

import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.dto.AppointmentResponse;
import com.hms.dto.CustomErrorResponse;
import com.hms.dto.DepartmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
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
			ResponseEntity<?> responseEntity = adminServiceProxy.adminRegister(admin);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In admin register contentUTF8: " + e.contentUTF8());
			log.debug("Error: In admin register toString: " + e.toString());
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
			ResponseEntity<JwtResponse> responseEntity = adminServiceProxy.adminLogin(request);
			System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				System.out.println(responseEntity.getBody().getClass());
				JwtResponse response1 = (JwtResponse) responseEntity.getBody();
				Cookie jwtCookie = new Cookie("authorization", response1.getToken());
				System.out.println("jwtCookie: " + jwtCookie.getValue());
				jwtCookie.setMaxAge(86400000);
				jwtCookie.setPath("/");
				response.addCookie(jwtCookie);

				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(true);
				myResponse.setUser(response1.getUser());
				myResponse.setToken(jwtCookie.getValue());
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			} else {
				Object errorMessage = null;
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				if (errorMessage instanceof String) {
					CustomErrorResponse customErrorResponse = new CustomErrorResponse();
					customErrorResponse.setSuccess(false);
					customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
					customErrorResponse.setError(errorMessage.toString());
					return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
				} else if (errorMessage instanceof Map) {
					ObjectMapper objectMapper = new ObjectMapper();
					String errorJsonString = objectMapper.writeValueAsString(errorMessage);
					JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);

					CustomErrorResponse customErrorResponse = new CustomErrorResponse();
					customErrorResponse.setSuccess(false);
					customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
					customErrorResponse.setError(errorResponse.getError());

					return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
				} else if (errorMessage instanceof JwtResponse) {
					ObjectMapper objectMapper = new ObjectMapper();
					String errorJsonString = objectMapper.writeValueAsString(errorMessage);
					JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
					// JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
					// JwtResponse.class);

					CustomErrorResponse customErrorResponse = new CustomErrorResponse();
					customErrorResponse.setSuccess(false);
					customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
					customErrorResponse.setError(errorResponse.getError());
					return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
				} else {
					return ResponseEntity.status(responseEntity.getStatusCodeValue()).body("Unknown Error");
				}
			}
		} catch (FeignException e) {
			log.debug("Error: In admin login: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PutMapping("/update-profile")
	public ResponseEntity<?> updateProfile(@RequestBody User request, HttpServletRequest httpRequest,
			HttpServletResponse response) throws Exception {
		try {
			log.debug("In update profile with data: " + request);
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
			ResponseEntity<JwtResponse> responseEntity = adminServiceProxy.updateProfile(request, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response1 = (JwtResponse) responseEntity.getBody();
				Cookie jwtCookie = new Cookie("authorization", response1.getToken());
				System.out.println("jwtCookie: " + jwtCookie.getValue());
				jwtCookie.setMaxAge(86400000);
				jwtCookie.setPath("/");
				response.addCookie(jwtCookie);

				JwtResponse myResponse = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (Exception e) {
			log.debug("Error: In update profile: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PostMapping("/department")
	public ResponseEntity<?> createDepartment(@RequestBody Department department, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In create department with data: " + department);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<DepartmentResponse> responseEntity = adminServiceProxy.createDepartment(department, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				DepartmentResponse myResponse = (DepartmentResponse) responseEntity.getBody();
				myResponse.setSuccess(true);
				myResponse.setDepartment(myResponse.getDepartment());
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			} else {
				Object errorMessage = null;
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				DepartmentResponse errorResponse = objectMapper.readValue(errorJsonString, DepartmentResponse.class);
				DepartmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						DepartmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In create department: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
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
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.getDepartmentById(id, token);
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("Error: In get department by id: " + e);
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/department/name/{name}")
	public ResponseEntity<?> getDepartmentsByName(@PathVariable String name, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In get department by name with name: " + name);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			return adminServiceProxy.getDepartmentsByName(name, token);
		} catch (Exception e) {
			log.debug("Error: In get department by name: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PutMapping("/department")
	public ResponseEntity<?> updateDepartment(@RequestBody Department department, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In update department with department data: " + department);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<DepartmentResponse> responseEntity = adminServiceProxy.updateDepartment(department, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				DepartmentResponse myResponse = (DepartmentResponse) responseEntity.getBody();
				myResponse.setSuccess(true);
				myResponse.setDepartment(myResponse.getDepartment());
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			} else {
				Object errorMessage = null;
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody().getError());
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				DepartmentResponse errorResponse = objectMapper.readValue(errorJsonString, DepartmentResponse.class);
				DepartmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						DepartmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In update department: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
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
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<DepartmentResponse> responseEntity = adminServiceProxy.deleteDepartment(id, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				DepartmentResponse myResponse = (DepartmentResponse) responseEntity.getBody();
				myResponse.setSuccess(true);
				myResponse.setDepartment(myResponse.getDepartment());
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			} else {
				Object errorMessage = null;
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				DepartmentResponse errorResponse = objectMapper.readValue(errorJsonString, DepartmentResponse.class);
				DepartmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						DepartmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In delete department: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PostMapping("/doctors")
	public ResponseEntity<?> registerDoctor(@RequestBody User doctor, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In doctor register with data: " + doctor);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.registerDoctor(doctor, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In doctor register contentUTF8: " + e.contentUTF8());
			log.debug("Error: In doctor register toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/doctors")
	public ResponseEntity<?> getAllDoctors(HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get all doctors");
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.getAllDoctors(token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In get all doctors contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get all doctors toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/doctors/{id}")
	public ResponseEntity<?> getDoctorById(@PathVariable("id") Long id, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In get doctor by id with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.getDoctorById(id, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In get doctor by id contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get doctor by id toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PutMapping("/doctors")
	public ResponseEntity<?> updateDoctor(@RequestBody User doctor, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In update doctor with doctor: " + doctor);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.updateDoctor(doctor, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In update doctor contentUTF8: " + e.contentUTF8());
			log.debug("Error: In update doctor toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@DeleteMapping("/doctors/{id}")
	public ResponseEntity<?> deleteDoctor(@PathVariable("id") Long id, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In delete doctor with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.deleteDoctor(id, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In delete doctor contentUTF8: " + e.contentUTF8());
			log.debug("Error: In delete doctor toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/patients")
	public ResponseEntity<?> getAllPatients(HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get all patients");
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.getAllPatients(token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In get all patients contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get all patients toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/patients/{id}")
	public ResponseEntity<?> getPatientById(@PathVariable("id") Long id, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In get patient by id with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.getPatientById(id, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In get patient by id contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get patient by id toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PutMapping("/patients")
	public ResponseEntity<?> updatePatient(@RequestBody User patient, HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In update patient with data: " + patient);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.updatePatient(patient, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In update patient contentUTF8: " + e.contentUTF8());
			log.debug("Error: In update patient toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@DeleteMapping("/patients/{id}")
	public ResponseEntity<?> deletePatient(@PathVariable("id") Long id, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In delete patient with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.deletePatient(id, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JwtResponse response = (JwtResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				JwtResponse errorResponse = objectMapper.readValue(errorJsonString, JwtResponse.class);
				JwtResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(), JwtResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In delete patient contentUTF8: " + e.contentUTF8());
			log.debug("Error: In delete patient toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/appointments")
	public ResponseEntity<?> getAllAppointments(HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get all appointments");
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.getAllAppointments(token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				AppointmentResponse response = (AppointmentResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				AppointmentResponse errorResponse = objectMapper.readValue(errorJsonString, AppointmentResponse.class);
				AppointmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						AppointmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In get all appointments contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get all appointments toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@GetMapping("/appointments/{id}")
	public ResponseEntity<?> getAppointmentById(@PathVariable("id") Long id, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In get appointment with id: " + id);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.getAppointmentById(id, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				AppointmentResponse response = (AppointmentResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				AppointmentResponse errorResponse = objectMapper.readValue(errorJsonString, AppointmentResponse.class);
				AppointmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						AppointmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In get appointment by id contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get appointment by id toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PostMapping("/appointments")
	public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In create appointment with data: " + appointment);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.createAppointment(appointment, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				AppointmentResponse response = (AppointmentResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				AppointmentResponse errorResponse = objectMapper.readValue(errorJsonString, AppointmentResponse.class);
				AppointmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						AppointmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In create appointment contentUTF8: " + e.contentUTF8());
			log.debug("Error: In create appointment toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

	@PutMapping("/appointments")
	public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In update appointment with data: " + appointment);
			Cookie[] cookies = httpRequest.getCookies();
			String token = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("authorization".equalsIgnoreCase(cookie.getName())) {
						token = cookie.getValue();
					}
				}
			}
			log.debug("JWT Token: " + token);
			ResponseEntity<?> responseEntity = adminServiceProxy.updateAppointment(appointment, token);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				AppointmentResponse response = (AppointmentResponse) responseEntity.getBody();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				System.out.println("responseEntity.getBody(): " + responseEntity.getBody());
				Object errorMessage = null;
				if (responseEntity.hasBody() && responseEntity.getBody() != null) {
					errorMessage = responseEntity.getBody();
				} else {
					errorMessage = "Unknown Error";
				}

				ObjectMapper objectMapper = new ObjectMapper();
				String errorJsonString = objectMapper.writeValueAsString(errorMessage);
				AppointmentResponse errorResponse = objectMapper.readValue(errorJsonString, AppointmentResponse.class);
				AppointmentResponse errorResponse1 = objectMapper.readValue(errorResponse.getError(),
						AppointmentResponse.class);

				CustomErrorResponse customErrorResponse = new CustomErrorResponse();
				customErrorResponse.setSuccess(false);
				customErrorResponse.setStatusCode(responseEntity.getStatusCodeValue());
				System.out.println("success: " + errorResponse1.isSuccess());
				System.out.println("error: " + errorResponse1.getError());
				customErrorResponse.setError(errorResponse1.getError());

				return ResponseEntity.status(responseEntity.getStatusCodeValue()).body(customErrorResponse);
			}
		} catch (FeignException e) {
			log.debug("Error: In update appointment contentUTF8: " + e.contentUTF8());
			log.debug("Error: In update appointment toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

}
