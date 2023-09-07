create database hospitaldb;

use hospitaldb;

CREATE TABLE departments (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    mobile VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    dp VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    department_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments (department_id)
);

CREATE TABLE appointments (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT,
    doctor_id BIGINT,
    is_admitted BOOLEAN NOT NULL DEFAULT false,
    appointment_status VARCHAR(255),
    days_spent INT,
    room_charge BIGINT,
    doctor_fee BIGINT NOT NULL,
    medicine_cost BIGINT NOT NULL,
    other_charges BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES users (user_id),
    FOREIGN KEY (doctor_id) REFERENCES users (user_id)
);

CREATE TABLE diseases_and_symptoms (
    appointment_id BIGINT,
    disease_or_symptom VARCHAR(255),
    PRIMARY KEY (appointment_id, disease_or_symptom),
    FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id)
);