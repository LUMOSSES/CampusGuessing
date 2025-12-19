package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class PointChange {
    private Integer earnPoints;
    private Integer pointBefore;
    private Integer pointAfter;
}