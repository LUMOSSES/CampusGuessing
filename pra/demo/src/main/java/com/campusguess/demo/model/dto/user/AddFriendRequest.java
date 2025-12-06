package com.campusguess.demo.model.dto.user;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AddFriendRequest {
    @NotNull(message = "好友ID不能为空")
    private Long friendUserId;
}