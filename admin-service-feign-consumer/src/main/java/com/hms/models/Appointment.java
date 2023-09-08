package com.hms.models;

import java.util.*;

public class Appointment {
    private long appointmentId;
	private User patient;
	private User doctor;
	private boolean isAdmitted;
	private String appointmentStatus;
    private List<String> diseasesAndSymptoms;
	private Integer daysSpent;
    private Long roomCharge;
    private Long doctorFee;
    private Long medicineCost;
    private Long otherCharges;
	private Date createdAt;
    private Date updatedAt;
    
    public Appointment() {

    }

    public Appointment(long appointmentId, User patient, User doctor, boolean isAdmitted, String appointmentStatus,
            List<String> diseasesAndSymptoms, Integer daysSpent, Long roomCharge, Long doctorFee, Long medicineCost,
            Long otherCharges) {
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
