package com.hms.dto;

import java.util.*;

import com.hms.models.Department;

public class DepartmentResponse {
	private boolean success;
	private String error;
	private Department department;
	private List<Department> departments;
	
	public DepartmentResponse() {
		super();
	}

	public DepartmentResponse(boolean success, String error, Department department,
			List<Department> departments) {
		super();
		this.success = success;
		this.error = error;
		this.department = department;
		this.departments = departments;
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

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}
	
}
