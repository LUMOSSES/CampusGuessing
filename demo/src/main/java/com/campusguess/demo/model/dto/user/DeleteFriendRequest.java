package com.campusguess.demo.model.dto.user;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class DeleteFriendRequest {
    @NotBlank(message = "好友用户名不能为空")
    private String friendUsername;
}
