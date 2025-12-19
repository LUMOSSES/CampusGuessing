package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class QuestionBaseDTO {
    private Long questionId;
    private String campus;
    private String difficulty;
    private String imageUrl;
    private CoordDTO correctCoord;

    public QuestionBaseDTO(Long questionId, String campus, String difficulty, String imageUrl, CoordDTO correctCoord) {
        this.questionId = questionId;
        this.campus = campus;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.correctCoord = correctCoord;
    }
}
