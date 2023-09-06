package com.hms.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.*;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="appointments")
@EntityListeners(AuditingEntityListener.class)
public class Appointment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "appointment_id")
	private long appointmentId;
	
	@ManyToOne
	@JoinColumn(name = "patient_id")
	private User patient;
	
	@ManyToOne
	@JoinColumn(name = "doctor_id")
	private User doctor;
	
	@Column(name = "is_admitted", nullable = false, updatable = true, columnDefinition = "BOOLEAN DEFAULT false")
	private boolean isAdmitted;
	
	@Column(name = "appointment_status")
	private String appointmentStatus;
	
	@ElementCollection
    @CollectionTable(name = "diseases_and_symptoms", joinColumns = @JoinColumn(name = "appointment_id"))
    @Column(name = "disease_or_symptom")
    private List<String> diseasesAndSymptoms;
	
	@Column(name = "days_spent")
	private Integer daysSpent;

	@Column(name = "room_charge")
    private Long roomCharge;

    @Column(name = "doctor_fee", nullable = false)
    private Long doctorFee;

    @Column(name="medicine_cost", nullable = false)
    private Long medicineCost;

    @Column(name="other_charges", nullable = false)
    private Long otherCharges;
    
    @CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
	private Date createdAt;
	
	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

	public Appointment() {
		super();
	}

	public Appointment(long appointmentId, User patient, User doctor, boolean isAdmitted, String appointmentStatus,
			List<String> diseasesAndSymptoms, Integer daysSpent, Long roomCharge, Long doctorFee, Long medicineCost,
			Long otherCharges) {
		super();
		this.appointmentId = appointmentId;
		this.patient = patient;
		this.doctor = doctor;
		this.isAdmitted = isAdmitted;
		this.appointmentStatus = appointmentStatus;
		this.diseasesAndSymptoms = diseasesAndSymptoms;
		this.daysSpent = daysSpent;
		this.roomCharge = roomCharge;
		this.doctorFee = doctorFee;
		this.medicineCost = medicineCost;
		this.otherCharges = otherCharges;
	}

	public long getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(long appointmentId) {
		this.appointmentId = appointmentId;
	}

	public User getPatient() {
		return patient;
	}

	public void setPatient(User patient) {
		this.patient = patient;
	}

	public User getDoctor() {
		return doctor;
	}

	public void setDoctor(User doctor) {
		this.doctor = doctor;
	}

	public boolean isAdmitted() {
		return isAdmitted;
	}

	public void setAdmitted(boolean isAdmitted) {
		this.isAdmitted = isAdmitted;
	}

	public String getAppointmentStatus() {
		return appointmentStatus;
	}

	public void setAppointmentStatus(String appointmentStatus) {
		this.appointmentStatus = appointmentStatus;
	}

	public List<String> getDiseasesAndSymptoms() {
		return diseasesAndSymptoms;
	}

	public void setDiseasesAndSymptoms(List<String> diseasesAndSymptoms) {
		this.diseasesAndSymptoms = diseasesAndSymptoms;
	}

	public Integer getDaysSpent() {
		return daysSpent;
	}

	public void setDaysSpent(Integer daysSpent) {
		this.daysSpent = daysSpent;
	}

	public Long getRoomCharge() {
		return roomCharge;
	}

	public void setRoomCharge(Long roomCharge) {
		this.roomCharge = roomCharge;
	}

	public Long getDoctorFee() {
		return doctorFee;
	}

	public void setDoctorFee(Long doctorFee) {
		this.doctorFee = doctorFee;
	}

	public Long getMedicineCost() {
		return medicineCost;
	}

	public void setMedicineCost(Long medicineCost) {
		this.medicineCost = medicineCost;
	}

	public Long getOtherCharges() {
		return otherCharges;
	}

	public void setOtherCharges(Long otherCharges) {
		this.otherCharges = otherCharges;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "Appointment [appointmentId=" + appointmentId + ", patient=" + patient + ", doctor=" + doctor
				+ ", isAdmitted=" + isAdmitted + ", appointmentStatus=" + appointmentStatus + ", diseasesAndSymptoms="
				+ diseasesAndSymptoms + ", daysSpent=" + daysSpent + ", roomCharge=" + roomCharge + ", doctorFee="
				+ doctorFee + ", medicineCost=" + medicineCost + ", otherCharges=" + otherCharges + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}
    
}
