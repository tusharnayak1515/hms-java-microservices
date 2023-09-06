package com.hms.dto;

import com.hms.models.User;

public class JwtResponse {
	private boolean success;
	private String error;
	private String token;
	private User user;
	
	public JwtResponse() {
		super();
	}

	public JwtResponse(boolean success, User user, String error, String token) {
		super();
		this.success = success;
		this.user = user;
		this.error = error;
		this.token = token;
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

	@Override
	public String toString() {
		return "JwtResponse [success=" + success + ", error=" + error + ", token=" + token + ", user=" + user + "]";
	}
	
}
