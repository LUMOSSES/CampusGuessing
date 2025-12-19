package com.campusguess.demo.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionListResponse {
    private Long total;
    private List<QuestionResponse> list;
}
