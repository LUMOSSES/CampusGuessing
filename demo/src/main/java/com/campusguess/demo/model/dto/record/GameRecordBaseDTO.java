package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class GameRecordBaseDTO {
    private Long recordId;
    private Long userId;
    private String username;
    private Integer totalQuestionNum;

    public GameRecordBaseDTO(Long recordId, Long userId, String username, Integer totalQuestionNum) {
        this.recordId = recordId;
        this.userId = userId;
        this.username = username;
        this.totalQuestionNum = totalQuestionNum;
    }
}
