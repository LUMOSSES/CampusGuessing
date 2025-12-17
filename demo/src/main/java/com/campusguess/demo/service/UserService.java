package com.campusguess.demo.service;

import com.campusguess.demo.model.dto.auth.LoginRequest;
import com.campusguess.demo.model.dto.auth.RegisterRequest;
import com.campusguess.demo.model.entity.User;

public interface UserService {
    User register(RegisterRequest request);

    User authenticate(LoginRequest request);

    User findByUsername(String username);

    User findById(Long id);

    void updateLastLogin(Long userId);
}