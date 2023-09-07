package com.hms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.models.Appointment;
import com.hms.models.User;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
	public List<Appointment> findBypatient(User patient);
	public List<Appointment> findBydoctor(User doctor);
}
