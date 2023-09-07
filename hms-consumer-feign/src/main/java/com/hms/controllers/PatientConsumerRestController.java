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
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.proxies.PatientServiceProxy;

import feign.FeignException;

@RestController
@Scope(value = "request")
@RequestMapping("/api")
public class PatientConsumerRestController {

    @Autowired
    private PatientServiceProxy patientServiceProxy;

    private Logger log = LoggerFactory.getLogger(AdminConsumerRestController.class);

    @PostMapping("/patient/register")
    public ResponseEntity<?> doctorRegister(@RequestBody User patient) throws Exception {
        try {
            log.debug("In patient register with data: " + patient);
            ResponseEntity<?> responseEntity = patientServiceProxy.patientRegister(patient);
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
            log.debug("Error: In patient register contentUTF8: " + e.contentUTF8());
            log.debug("Error: In patient register toString: " + e.toString());
            JwtResponse myResponse = new JwtResponse();
            myResponse.setSuccess(false);
            myResponse.setError(e.contentUTF8());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
        }
    }

    @PostMapping("/patient/login")
	public ResponseEntity<?> doctorLogin(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse response) throws Exception {
		try {
			log.debug("In patient login with data: " + request.getMobile());
			ResponseEntity<JwtResponse> responseEntity = patientServiceProxy.patientLogin(request);
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
			log.debug("Error: In patient login: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

    @PutMapping("/patient/update-profile")
	public ResponseEntity<?> updateProfile(@RequestBody User request, HttpServletRequest httpRequest,
			HttpServletResponse response) throws Exception {
		try {
			log.debug("In update profile with data: " + request);
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
			ResponseEntity<JwtResponse> responseEntity = patientServiceProxy.updateProfile(request, token);
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

    @GetMapping("/patient/doctors/{id}")
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
			ResponseEntity<?> responseEntity = patientServiceProxy.getDoctorById(id, token);
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

    @GetMapping("/patient/appointments")
	public ResponseEntity<?> getMyAppointments(HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get my appointments");
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
			ResponseEntity<?> responseEntity = patientServiceProxy.getMyAppointments(token);
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
			log.debug("Error: In get my appointments contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get my appointments toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

    @GetMapping("/patient/appointments/{id}")
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
			ResponseEntity<?> responseEntity = patientServiceProxy.getAppointmentById(id, token);
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

    @PostMapping("/patient/appointments")
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
			ResponseEntity<?> responseEntity = patientServiceProxy.createAppointment(appointment, token);
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
}
