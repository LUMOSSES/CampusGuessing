package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class QuestionDetailDTO {
    private QuestionBaseDTO questionBase;
    private UserAnswerInfoDTO userAnswerInfo;

    public QuestionDetailDTO(QuestionBaseDTO questionBase, UserAnswerInfoDTO userAnswerInfo) {
        this.questionBase = questionBase;
        this.userAnswerInfo = userAnswerInfo;
    }
}
