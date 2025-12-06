package com.campusguess.demo.controller;

import com.campusguess.demo.model.dto.*;
import com.campusguess.demo.model.dto.question.*;
import com.campusguess.demo.model.entity.User;
import com.campusguess.demo.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // 创建问题
    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(
            @Valid @RequestBody QuestionCreate dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.createQuestion(dto, user));
    }

    // 获取问题列表（支持分页和筛选）
    @GetMapping
    public ResponseEntity<Page<QuestionResponse>> getQuestions(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer campusId,
            @RequestParam(required = false) String search) {

        // 构建筛选条件
        Map<String, Object> filters = new java.util.HashMap<>();
        if (difficulty != null)
            filters.put("difficulty", difficulty);
        if (campusId != null)
            filters.put("campusId", campusId);
        if (search != null)
            filters.put("search", search);

        return ResponseEntity.ok(questionService.getQuestions(pageable, filters));
    }

    // 获取单个问题详情
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    // 更新问题
    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.updateQuestion(id, dto, user));
    }

    // 删除问题
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        questionService.deleteQuestion(id, user);
        return ResponseEntity.ok().build();
    }

    // 回答问题
    @PostMapping("/{id}/answer")
    public ResponseEntity<Map<String, Object>> answerQuestion(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.answerQuestion(id, latitude, longitude, user));
    }

    // 获取随机问题
    @GetMapping("/random")
    public ResponseEntity<QuestionResponse> getRandomQuestion(
            @RequestParam(required = false) Integer campusId) {
        return ResponseEntity.ok(questionService.getRandomQuestion(campusId));
    }

    // 获取用户创建的问题
    @GetMapping("/my-questions")
    public ResponseEntity<Page<QuestionResponse>> getMyQuestions(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(questionService.getUserQuestions(user.getId(), pageable));
    }
}
