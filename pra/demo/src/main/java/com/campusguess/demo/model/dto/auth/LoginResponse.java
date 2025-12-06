package com.campusguess.demo.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.campusguess.demo.model.dto.user.UserInfoResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private LocalDateTime expireTime;
    private UserInfoResponse userInfo;
}