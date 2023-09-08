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
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.proxies.DoctorServiceProxy;

import feign.FeignException;

@RestController
@Scope(value = "request")
@RequestMapping("/api")
public class DoctorConsumerRestController {

    @Autowired
    private DoctorServiceProxy doctorServiceProxy;

    private Logger log = LoggerFactory.getLogger(DoctorConsumerRestController.class);

    @PostMapping("/doctor/register")
    public ResponseEntity<?> doctorRegister(@RequestBody User doctor) throws Exception {
        try {
            log.debug("In doctor register with data: " + doctor);
            ResponseEntity<?> responseEntity = doctorServiceProxy.doctorRegister(doctor);
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

    @PostMapping("/doctor/login")
	public ResponseEntity<?> doctorLogin(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse response) throws Exception {
		try {
			log.debug("In doctor login with data: " + request.getMobile());
			ResponseEntity<JwtResponse> responseEntity = doctorServiceProxy.doctorLogin(request);
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
			log.debug("Error: In doctor login: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

    @PutMapping("/doctor/update-profile")
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
			ResponseEntity<JwtResponse> responseEntity = doctorServiceProxy.updateProfile(request, token);
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

    @GetMapping("/doctor/patients")
	public ResponseEntity<?> getMyPatients(HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get my patients");
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
			ResponseEntity<?> responseEntity = doctorServiceProxy.getMyPatients(token);
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
			log.debug("Error: In get my patients contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get my patients toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

    @GetMapping("/doctor/patients/discharged")
	public ResponseEntity<?> getMyDischargedPatients(HttpServletRequest httpRequest) throws Exception {
		try {
			log.debug("In get my discharged patients");
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
			ResponseEntity<?> responseEntity = doctorServiceProxy.getMyDischargedPatients(token);
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
			log.debug("Error: In get my discharged patients contentUTF8: " + e.contentUTF8());
			log.debug("Error: In get my discharged patients toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}

    @GetMapping("/doctor/appointments")
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
			ResponseEntity<?> responseEntity = doctorServiceProxy.getMyAppointments(token);
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

    @PutMapping("/doctor/appointments")
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
			ResponseEntity<?> responseEntity = doctorServiceProxy.updateAppointment(appointment, token);
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

    @DeleteMapping("/doctor/appointments/{id}")
	public ResponseEntity<?> deleteAppointment(@PathVariable("id") Long id, HttpServletRequest httpRequest)
			throws Exception {
		try {
			log.debug("In delete appointment with id: " + id);
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
			ResponseEntity<?> responseEntity = doctorServiceProxy.deleteAppointment(id, token);
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
			log.debug("Error: In delete appointment contentUTF8: " + e.contentUTF8());
			log.debug("Error: In delete appointment toString: " + e.toString());
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.contentUTF8());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
}
