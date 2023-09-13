package com.hms.services;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.hms.models.Department;
import com.hms.repositories.DepartmentRepo;

@Service(value = "departmentService")
@Scope(value = "singleton")
public class DepartmentService {
	
	@Autowired
	@Qualifier("departmentRepo")
	private DepartmentRepo departmentRepo;
	
	public List<Department> findAll() {
		return this.departmentRepo.findAll();
	}
	
	public Department findById(Long id) {
		Optional<Department> optionalDepartment =  this.departmentRepo.findById(id);
		return optionalDepartment.isPresent() ? optionalDepartment.get() : null;
	}
	
	public Department findByName(String name) {
		return this.departmentRepo.findBydepartmentName(name);
	}
	
	public Department createDepartment(Department department) {
		return this.departmentRepo.save(department); 
    }
	
	public Department updateDepartment(Department updatedDepartment) {
		Optional<Department> optionalDepartment = this.departmentRepo.findById(updatedDepartment.getDepartmentId());
		if (optionalDepartment.isPresent()) {
			Department department = optionalDepartment.get();
			department.setDepartmentName(updatedDepartment.getDepartmentName());
            return this.departmentRepo.save(department);
        } 
		else {
            throw new IllegalArgumentException("Department with ID " + updatedDepartment.getDepartmentId() + " not found.");
        }
    }
	
	public void deleteDepartment(Long id) {
        Optional<Department> optionalDepartment = this.departmentRepo.findById(id);

        if (optionalDepartment.isPresent()) {
            this.departmentRepo.deleteById(id);
        } else {
            throw new IllegalArgumentException("Department with ID " + id + " not found.");
        }
    } 
}
