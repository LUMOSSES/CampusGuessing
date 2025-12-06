package com.campusguess.demo.model.dto.question;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QuestionResponse {
    private Long id;
    private String title;
    private String description;
    private String mapImageUrl;
    private String buildingName;
    private Integer hintLevel;
    private String hint;
    private String difficultyLevel;
    private String creatorName;
    private Integer playCount;
    private Double correctRate;
    private LocalDateTime createdAt;

    // 不返回正确答案坐标，防止作弊
}