package com.hms.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.repositories.AppointmentRepo;

@Service
public class AppointmentService {
	
	@Autowired
	private AppointmentRepo appointmentRepo;
	
	public List<Appointment> findAll() {
		return this.appointmentRepo.findAll();
	}
	
	public List<Appointment> findByPatient(User patient) {
		return this.appointmentRepo.findBypatient(patient);
	}
	
	public List<Appointment> findByDoctor(User doctor) {
		return this.appointmentRepo.findBydoctor(doctor);
	}
	
	public Appointment findById(Long id) {
		Optional<Appointment> optionalAppointment =  this.appointmentRepo.findById(id);
		return optionalAppointment.isPresent() ? optionalAppointment.get() : null;
	}
	
	public Appointment createAppointment(Appointment appointment) {
		return this.appointmentRepo.save(appointment); 
    }
	
	public Appointment updateAppointment(Appointment updatedAppointment) {
		System.out.println("updatedAppointment: "+updatedAppointment);
		Optional<Appointment> optionalAppointment = this.appointmentRepo.findById(updatedAppointment.getAppointmentId());
		if (optionalAppointment.isPresent()) {
			Appointment appointment = optionalAppointment.get();
			appointment.setAdmitted(updatedAppointment.isAdmitted());
			appointment.setDaysSpent(updatedAppointment.getDaysSpent());
			appointment.setRoomCharge(updatedAppointment.getRoomCharge());
			appointment.setDoctorFee(updatedAppointment.getDoctorFee());
			appointment.setMedicineCost(updatedAppointment.getMedicineCost());
			appointment.setOtherCharges(updatedAppointment.getOtherCharges());
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
