package com.hms.dto;

import java.util.*;

import com.hms.models.Department;

public class DepartmentResponse {
	private boolean success;
	private String error;
	private String token;
	private Department department;
	private List<Department> departments;
	
	public DepartmentResponse() {
		super();
	}

	public DepartmentResponse(boolean success, String error, String token, Department department,
			List<Department> departments) {
		super();
		this.success = success;
		this.error = error;
		this.token = token;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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
