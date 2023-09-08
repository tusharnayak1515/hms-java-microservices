package com.hms.controllers;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.hms.models.Department;
import com.hms.models.User;
import com.hms.services.AppointmentService;
import com.hms.services.CustomUserDetailsService;
import com.hms.services.DepartmentService;
import com.hms.utils.JwtUtil;

@RestController
@Scope(value = "request")
public class DoctorController {
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	private AppointmentService appointmentService;
	
	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
    private JwtUtil jwtUtil;
	
	private Logger log = LoggerFactory.getLogger(DoctorController.class);
	
	@PostMapping(value = "/register", produces = "application/json")
	public ResponseEntity<?> applyJob(@RequestBody User doctor) throws Exception {
		try {
			log.debug("In doctor-service doctor register with data: "+doctor);
			
			if (doctor.getUsername().replaceAll("\\s+", "").length() == 0) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Username cannot be empty");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }
			
			if (doctor.getMobile().replaceAll("\\s+", "").length() == 0) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Mobile cannot be empty");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }

            String mobileRegex = "^[0-9]{10}$";
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            Matcher mobileMatcher = mobilePattern.matcher(doctor.getMobile());

            if (!mobileMatcher.matches()) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Invalid mobile");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }

            if (doctor.getPassword().replaceAll("\\s+", "").length() == 0) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Password cannot be empty");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }

            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
            Pattern passwordPattern = Pattern.compile(passwordRegex);
            Matcher passwordMatcher = passwordPattern.matcher(doctor.getPassword());

            if (!passwordMatcher.matches()) {
                JwtResponse myResponse = new JwtResponse();
                myResponse.setSuccess(false);
                myResponse.setError("Enter a strong password");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(myResponse);
            }
			
			User isUser1 = customUserDetailsService.findByMobile(doctor.getMobile());
			
			if(isUser1 != null) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("User already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			Department department = this.departmentService.findById(doctor.getDepartment().getDepartmentId());
			if(department == null) {
				JwtResponse myResponse = new JwtResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Department not found");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
			}
			
			doctor.setRole("doctor");
			doctor.setStatus("pending");
			doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
			doctor = customUserDetailsService.createUser(doctor);
			log.debug("In doctor-service doctor register with doctor data: "+doctor);
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
	public ResponseEntity<?> doctorLogin(@RequestBody LoginRequest request, HttpServletResponse response) throws Exception {
		try {
			log.debug("In doctor-service doctor login with mobile: "+request.getMobile());
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
	        log.debug("In doctor-service doctor login with doctor data: "+user);
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
			
			if(request.getDepartment() != null) {
				Department department = this.departmentService.findById(request.getDepartment().getDepartmentId());
				if(department == null) {
					JwtResponse myResponse = new JwtResponse();
					myResponse.setSuccess(false);
					myResponse.setError("Department not found");
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myResponse);
				}				
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
			log.debug("In doctor-service update profile with user data: "+user);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);			
		} catch (Exception e) {
			JwtResponse myResponse = new JwtResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
    }
	
	@GetMapping("/patients")
	public ResponseEntity<?> getMyPatients(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			List<Appointment> appointments = this.appointmentService.findByDoctor(user);
			
			List<User> patients = new ArrayList<User>();
			
			Iterator<Appointment> iterator = appointments.iterator();
			while (iterator.hasNext()) {
				Appointment element = iterator.next();
				if(!element.getAppointmentStatus().equalsIgnoreCase("discharged") && !element.getAppointmentStatus().equalsIgnoreCase("pending")) {
					User patient = element.getPatient();
					patient.setPassword(null);
					patients.add(patient);					
				}
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
	
	@GetMapping("/patients/discharged")
	public ResponseEntity<?> getMyDischargedPatients(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			List<Appointment> appointments = this.appointmentService.findByDoctor(user);
			
			List<User> patients = new ArrayList<User>();
			
			Iterator<Appointment> iterator = appointments.iterator();
			while (iterator.hasNext()) {
				Appointment element = iterator.next();
				if(element.getAppointmentStatus().equalsIgnoreCase("discharged")) {
					User patient = element.getPatient();
					patient.setPassword(null);
					patients.add(patient);					
				}
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
	
	@GetMapping("/appointments")
	public ResponseEntity<?> getMyAppointments(HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			List<Appointment> appointments = this.appointmentService.findByDoctor(user);
			
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
	
	@PutMapping("/appointments")
	public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("doctor")) {
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
			
			appointment.getDoctor().setPassword(null);
			appointment.getPatient().setPassword(null);
			
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
	
	@DeleteMapping("/appointments/{id}")
	public ResponseEntity<?> deleteAppointment(@PathVariable("id") Long id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String mobile = authentication.getName();
			User user = this.customUserDetailsService.findByMobile(mobile);
			
			if(!user.getRole().equals("doctor")) {
				AppointmentResponse myResponse = new AppointmentResponse();
	            myResponse.setSuccess(false);
	            myResponse.setError("Not Allowed");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			Appointment appointment = this.appointmentService.findById(id);
			if(appointment == null) {
				AppointmentResponse myResponse = new AppointmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Appointment not found");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			if(appointment.getDoctor().getUserId() != user.getUserId()) {
				AppointmentResponse myResponse = new AppointmentResponse();
				myResponse.setSuccess(false);
				myResponse.setError("Not Allowed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myResponse);
			}
			
			this.appointmentService.deleteAppointment(id);
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(true);
			return ResponseEntity.status(HttpStatus.OK).body(myResponse);
		} catch (Exception e) {
			AppointmentResponse myResponse = new AppointmentResponse();
			myResponse.setSuccess(false);
			myResponse.setError(e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myResponse);
		}
	}
}
