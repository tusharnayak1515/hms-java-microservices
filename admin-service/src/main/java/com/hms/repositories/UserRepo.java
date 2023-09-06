package com.hms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.models.User;

public interface UserRepo extends JpaRepository<User,Long> {
    public User findByMobile(String mobile);
    public User findByUsername(String username);
}
