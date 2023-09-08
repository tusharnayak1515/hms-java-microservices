package com.hms.models;

import java.util.*;

public class Department {
    private long departmentId;
	private String departmentName;
    private List<User> users;
	private Date createdAt;
    private Date updatedAt;

    public Department() {
        
    }

    public Department(long departmentId, String departmentName, List<User> users) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.users = users;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
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
        return "Department [departmentId=" + departmentId + ", departmentName=" + departmentName + ", users=" + users
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

    
}
