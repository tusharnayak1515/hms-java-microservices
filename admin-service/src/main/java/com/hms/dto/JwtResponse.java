package com.hms.dto;

import java.util.List;

import com.hms.models.User;

public class JwtResponse {
	private boolean success;
	private String error;
	private String token;
	private User user;
	private List<User> users;
	
	public JwtResponse() {
		super();
	}

	public JwtResponse(boolean success, String error, String token, User user, List<User> users) {
		super();
		this.success = success;
		this.error = error;
		this.token = token;
		this.user = user;
		this.users = users;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	
}
