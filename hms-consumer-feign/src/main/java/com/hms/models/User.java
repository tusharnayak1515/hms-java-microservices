package com.hms.models;

import java.util.*;

public class User {
	private long userId;
    private String username;
    private String mobile;
    private String address;
    private String dp;
    private String password;
    private String role;
    private Department department;
	private Date createdAt;
    private Date updatedAt;

    public User() {
        super();
    }

    public User(long userId, String username, String mobile, String address, String dp, String password, String role,
            Department department) {
        this.userId = userId;
        this.username = username;
        this.mobile = mobile;
        this.address = address;
        this.dp = dp;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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
        return "User [userId=" + userId + ", username=" + username + ", mobile=" + mobile + ", address=" + address
                + ", dp=" + dp + ", password=" + password + ", role=" + role + ", department=" + department
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

    
}
