package com.hms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.models.Department;

@Repository
public interface DepartmentRepo extends JpaRepository<Department, Long> {
	public Department findBydepartmentName(String name);
}
