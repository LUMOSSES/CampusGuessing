package com.campusguess.demo.model.dto.user;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String role;
    private String phone;
    private Integer points;

    public UserInfoResponse(Long userId, String username, String role, String phone, Integer points) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.phone = phone;
        this.points = points;
    }
}