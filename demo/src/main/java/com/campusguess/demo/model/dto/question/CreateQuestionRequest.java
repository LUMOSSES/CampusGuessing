package com.campusguess.demo.model.dto.question;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateQuestionRequest {
    @NotBlank(message = "campus 不能为空")
    private String campus;

    @NotBlank(message = "difficulty 不能为空")
    private String difficulty;

    @NotBlank(message = "key 不能为空")
    private String key;

    private CorrectCoord correctCoord;

    // 可选：题目标题、内容、答案
    private String title;
    private String content;
    private String answer;

    @Data
    public static class CorrectCoord {
        private Double lon;
        private Double lat;
    }
}
