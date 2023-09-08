package com.hms.dto;

import java.util.List;

import com.hms.models.Appointment;

public class AppointmentResponse {
	private boolean success;
	private String error;
	private Appointment appointment;
	private List<Appointment> appointments;
	
	public AppointmentResponse() {
		super();
	}

	public AppointmentResponse(boolean success, String error, Appointment appointment, List<Appointment> appointments) {
		super();
		this.success = success;
		this.error = error;
		this.appointment = appointment;
		this.appointments = appointments;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public List<Appointment> getAppointments() {
		return appointments;
	}

	public void setAppointments(List<Appointment> appointments) {
		this.appointments = appointments;
	}

	@Override
	public String toString() {
		return "AppointmentResponse [success=" + success + ", error=" + error + ", appointment=" + appointment
				+ ", appointments=" + appointments + "]";
	}
	
}
