package com.campusguess.demo.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointChangeRequest {
    @NotNull
    private Integer pointChange;
}
