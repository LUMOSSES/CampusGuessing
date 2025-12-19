package com.campusguess.demo.controller;

import com.campusguess.demo.exception.BusinessException;
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
@RequestMapping
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 创建题目
     * POST /users/{username}/questions
     */
    @PostMapping("/users/{username}/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @PathVariable String username,
            @Valid @RequestBody CreateQuestionRequest request) {
        try {
            QuestionResponse resp = questionService.createQuestion(username, request);
            return ResponseEntity.ok(ApiResponse.success("题目创建成功", resp));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 获取题目详情
     * GET /questions/{id}
     */
    @GetMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(@PathVariable Long id) {
        try {
            QuestionResponse resp = questionService.getQuestion(id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", resp));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 分页查询题库
     * GET /questions
     */
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<QuestionListResponse>> listQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            QuestionListResponse resp = questionService.listQuestions(pageable);
            return ResponseEntity.ok(ApiResponse.success("查询成功", resp));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 查询某用户的题目列表
     * GET /users/{username}/questions
     */
    @GetMapping("/users/{username}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponse>> listByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            QuestionListResponse resp = questionService.listByUser(username, pageable);
            return ResponseEntity.ok(ApiResponse.success("查询成功", resp));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 删除题目（仅作者可删除）
     * DELETE /users/{username}/questions/{questionId}
     */
    @DeleteMapping("/users/{username}/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable String username,
            @PathVariable Long questionId) {
        try {
            questionService.deleteQuestion(username, questionId);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }
}
