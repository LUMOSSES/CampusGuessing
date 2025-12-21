package com.campusguess.demo.controller;

import com.campusguess.demo.model.dto.question.CreateQuestionRequest;
import com.campusguess.demo.model.dto.question.QuestionListResponse;
import com.campusguess.demo.model.dto.question.QuestionResponse;
import com.campusguess.demo.model.dto.response.ApiResponse;
import com.campusguess.demo.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /** 创建题目 */
    @PostMapping("/users/{username}/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @PathVariable String username,
            @Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse resp = questionService.createQuestion(username, request);
        return ResponseEntity.ok(ApiResponse.success("题目创建成功", resp));
    }

    /** 获取题目详情 */
    @GetMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(@PathVariable Long id) {
        QuestionResponse resp = questionService.getQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("查询成功", resp));
    }

    /** 分页查询题库 */
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<QuestionListResponse>> listQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        QuestionListResponse resp = questionService.listQuestions(pageable);
        return ResponseEntity.ok(ApiResponse.success("查询成功", resp));
    }

    /** 查询某用户的题目列表 */
    @GetMapping("/users/{username}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponse>> listByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        QuestionListResponse resp = questionService.listByUser(username, pageable);
        return ResponseEntity.ok(ApiResponse.success("查询成功", resp));
    }

    /** 删除题目（仅作者可删除） */
    @DeleteMapping("/users/{username}/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable String username,
            @PathVariable Long questionId) {
        questionService.deleteQuestion(username, questionId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
