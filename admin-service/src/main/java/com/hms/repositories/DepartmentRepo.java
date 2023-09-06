package com.hms.repositories;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.models.Department;

public interface DepartmentRepo extends JpaRepository<Department, Long> {
	public Department findBydepartmentName(String name);
}
