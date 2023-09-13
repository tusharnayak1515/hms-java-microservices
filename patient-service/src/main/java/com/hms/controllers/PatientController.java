package com.hms.controllers;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.AppointmentResponse;
import com.hms.dto.JwtResponse;
import com.hms.dto.LoginRequest;
import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.services.AppointmentService;
import com.hms.services.CustomUserDetailsService;
import com.hms.utils.JwtUtil;

@RestController
@Scope(value = "request")
public class PatientController {
	
	@Autowired
	@Qualifier("userService")
	private CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	@Qualifier("appointmentService")
	private AppointmentService appointmentService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
    private JwtUtil jwtUtil;
	
	private Logger log = LoggerFactory.getLogger(PatientController.class);
	
	@PostMapping(value = "/register", produces = "application/json")
	public ResponseEntity<?> patientRegister(@RequestBody User patient) throws Exception {
		try {
			log.debug("In patient-service patient register with data: "+patient);
			
			if (patient.getUsername().replaceAll("\\s+", "").length() == 0) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Username cannot be empty");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }
			
			if (patient.getMobile().replaceAll("\\s+", "").length() == 0) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Mobile cannot be empty");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }

            String mobileRegex = "^[0-9]{10}$";
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            Matcher mobileMatcher = mobilePattern.matcher(patient.getMobile());

            if (!mobileMatcher.matches()) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Invalid mobile");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }

            if (patient.getPassword().replaceAll("\\s+", "").length() == 0) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Password cannot be empty");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }

            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
            Pattern passwordPattern = Pattern.compile(passwordRegex);
            Matcher passwordMatcher = passwordPattern.matcher(patient.getPassword());

            if (!passwordMatcher.matches()) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Enter a strong password");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }
			
			User isUser1 = customUserDetailsService.findByMobile(patient.getMobile());
			User isUser2 = customUserDetailsService.findByUsername(patient.getUsername());
			
			if(isUser1 != null && isUser2 != null) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("User already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			patient.setRole("patient");
			patient.setStatus("pending");
			patient.setPassword(passwordEncoder.encode(patient.getPassword()));
			patient = customUserDetailsService.createUser(patient);
			log.debug("In patient-service patient register with patient data: "+patient);
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
	
	@PostMapping("/login")
	public ResponseEntity<?> patientLogin(@RequestBody LoginRequest request, HttpServletResponse response) throws Exception {
		try {
			log.debug("In patient-service patient login with mobile: "+request.getMobile());
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
	        log.debug("In patient-service patient login with patient data: "+user);
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
			log.debug("In patient-service update profile with user data: "+user);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
    }
	
	@GetMapping("/appointments")
	public ResponseEntity<?> getMyAppointments(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			List<Appointment> appointments = this.appointmentService.findByPatient(user);
			
			Iterator<Appointment> iterator = appointments.iterator();
			while (iterator.hasNext()) {
				Appointment element = iterator.next();
                element.getDoctor().setPassword(null);
                element.getPatient().setPassword(null);
            }
			
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
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			Appointment appointment = this.appointmentService.findById(id);
			
			if(appointment == null) {
				AppointmentResponse myResponse = new AppointmentResponse();
				myResponse.setSuccess(true);
				myResponse.setAppointment(appointment);
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			}
			
			if(appointment.getPatient().getUserId() != user.getUserId()) {
				AppointmentResponse myResponse = new AppointmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Not allowed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			appointment.getPatient().setPassword(null);
			appointment.getDoctor().setPassword(null);
			
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
			appointment.setPatient(user);
			appointment.setAppointmentStatus("pending");
			appointment.setDoctorFee(0L);
			appointment.setMedicineCost(0L);
			appointment.setOtherCharges(0L);
			appointment = this.appointmentService.createAppointment(appointment);
			
			appointment.getPatient().setPassword(null);
			appointment.getDoctor().setPassword(null);
			user.setPassword(null);
			
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
	
	@GetMapping("/doctors/{id}")
	public ResponseEntity<?> getDoctorById(@PathVariable("id") Long id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			User doctor = this.customUserDetailsService.findById(id);
			
			if(doctor == null) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(true);
				myResponse.setUser(doctor);
				return ResponseEntity.status(HttpStatus.OK).body(myResponse);
			}
			
			List<Appointment> appointments = this.appointmentService.findByPatient(user);
			Iterator<Appointment> iterator = appointments.iterator();
			boolean found = false;
			while (iterator.hasNext()) {
				Appointment element = iterator.next();
                if(element.getDoctor().getUserId() == doctor.getUserId()) {
                	found = true;
                	break;
                }
            }
			
			if(found == false) {
				JwtResponse myResponse = new JwtResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Not allowed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
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
}
