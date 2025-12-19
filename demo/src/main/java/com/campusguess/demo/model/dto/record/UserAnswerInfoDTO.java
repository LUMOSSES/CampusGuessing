package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class UserAnswerInfoDTO {
    private CoordDTO userCoord;
    private Integer singleScore;

    public UserAnswerInfoDTO(CoordDTO userCoord, Integer singleScore) {
        this.userCoord = userCoord;
        this.singleScore = singleScore;
    }
}
