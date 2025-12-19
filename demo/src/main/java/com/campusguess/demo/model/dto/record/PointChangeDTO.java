package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class PointChangeDTO {
    private Integer earnPoints;
    private Integer pointBefore;
    private Integer pointAfter;

    public PointChangeDTO(Integer earnPoints, Integer pointBefore, Integer pointAfter) {
        this.earnPoints = earnPoints;
        this.pointBefore = pointBefore;
        this.pointAfter = pointAfter;
    }
}
