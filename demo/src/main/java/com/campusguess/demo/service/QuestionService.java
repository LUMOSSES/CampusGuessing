package com.campusguess.demo.service;

import com.campusguess.demo.model.dto.question.CreateQuestionRequest;
import com.campusguess.demo.model.dto.question.QuestionListResponse;
import com.campusguess.demo.model.dto.question.QuestionResponse;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
    QuestionResponse createQuestion(String username, CreateQuestionRequest request);

    QuestionResponse getQuestion(Long id);

    QuestionListResponse listQuestions(Pageable pageable);

    QuestionListResponse listByUser(String username, Pageable pageable);

    void deleteQuestion(String username, Long questionId);
}
