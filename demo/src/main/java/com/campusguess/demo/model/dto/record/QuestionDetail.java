package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class QuestionDetail {
    private Long questionId;
    private String campus;
    private String difficulty;
    private String imageUrl;
    private UserCoord correctCoord;
    private UserAnswerInfo userAnswerInfo;

    @Data
    public static class UserCoord {
        private Double lon;
        private Double lat;
    }

    @Data
    public static class UserAnswerInfo {
        private UserCoord userCoord;
        private Integer singleScore;
    }
}