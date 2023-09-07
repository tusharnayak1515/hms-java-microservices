package com.hms.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hms.models.Appointment;
import com.hms.repositories.AppointmentRepo;

@Service
public class AppointmentService {
	
	@Autowired
	private AppointmentRepo appointmentRepo;
	
	public List<Appointment> findAll() {
		return this.appointmentRepo.findAll();
	}
	
	public Appointment findById(Long id) {
		Optional<Appointment> optionalAppointment =  this.appointmentRepo.findById(id);
		return optionalAppointment.isPresent() ? optionalAppointment.get() : null;
	}
	
	public Appointment createAppointment(Appointment appointment) {
		return this.appointmentRepo.save(appointment); 
    }
	
	public Appointment updateAppointment(Appointment updatedAppointment) {
		Optional<Appointment> optionalAppointment = this.appointmentRepo.findById(updatedAppointment.getAppointmentId());
		if (optionalAppointment.isPresent()) {
			Appointment appointment = optionalAppointment.get();
			appointment.setAppointmentStatus(updatedAppointment.getAppointmentStatus());
            return this.appointmentRepo.save(appointment);
        } 
		else {
            throw new IllegalArgumentException("Appointment with ID " + updatedAppointment.getAppointmentId() + " not found.");
        }
    }
	
	public void deleteAppointment(Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentRepo.findById(id);

        if (optionalAppointment.isPresent()) {
            this.appointmentRepo.deleteById(id);
        } else {
            throw new IllegalArgumentException("Appointment with ID " + id + " not found.");
        }
    } 
}
