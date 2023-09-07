package com.hms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.models.Department;
import com.hms.models.User;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    public User findByMobile(String mobile);
    public User findByUsername(String username);
    public User findBydepartment(Department department);
    public List<User> findByrole(String role);
}
