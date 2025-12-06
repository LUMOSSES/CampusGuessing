package com.campusguess.demo.service.impl;

import com.campusguess.demo.model.dto.*;
import com.campusguess.demo.model.dto.question.*;
import com.campusguess.demo.model.entity.Question;
import com.campusguess.demo.model.entity.User;
import com.campusguess.demo.repository.QuestionRepository;
import com.campusguess.demo.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    // 坐标计算相关常量
    private static final double EARTH_RADIUS = 6371000; // 地球半径（米）
    private static final double CORRECT_DISTANCE_THRESHOLD = 50.0; // 正确距离阈值（米）

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionCreate dto, User creator) {
        Question question = new Question();
        convertDtoToEntity(dto, question);
        question.setCreator(creator);
        question.setCampusId(dto.getCampusId() != null ? dto.getCampusId() : 1); // 默认珠海校区

        Question saved = questionRepository.save(question);
        return convertToResponseDTO(saved);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionDTO dto, User user) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("问题不存在"));

        // 检查权限：只有创建者或管理员可以修改
        if (!question.getCreator().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new SecurityException("没有权限修改此问题");
        }

        convertDtoToEntity(dto, question);
        Question updated = questionRepository.save(question);
        return convertToResponseDTO(updated);
    }

    @Override
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("问题不存在"));

        // 增加游玩次数
        question.setPlayCount(question.getPlayCount() + 1);
        questionRepository.save(question);

        return convertToResponseDTO(question);
    }

    @Override
    @Transactional
    public Map<String, Object> answerQuestion(Long questionId, Double userLat, Double userLng, User user) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("问题不存在"));

        // 计算用户答案与正确答案的距离
        double distance = calculateDistance(
                userLat, userLng,
                question.getLatitude(), question.getLongitude());

        boolean isCorrect = distance <= CORRECT_DISTANCE_THRESHOLD;

        // 更新正确率
        if (isCorrect) {
            updateCorrectRate(question, true);
        } else {
            updateCorrectRate(question, false);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("distance", distance);
        result.put("correctLocation", isCorrect ? question.getCorrectLocation() : null);
        result.put("buildingName", question.getBuildingName());
        result.put("hint", question.getHint());

        // 这里可以记录用户答题记录，需要UserAnswerRecord实体

        return result;
    }

    @Override
    public QuestionResponse getRandomQuestion(Integer campusId) {
        return questionRepository.findRandomQuestion(campusId != null ? campusId : 1)
                .map(this::convertToResponse)
                .orElseThrow(() -> new EntityNotFoundException("没有可用的问题"));
    }

    // 辅助方法：计算两个坐标点之间的距离（Haversine公式）
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    // 更新正确率
    private void updateCorrectRate(Question question, boolean isCorrect) {
        int totalPlays = question.getPlayCount();
        double currentRate = question.getCorrectRate();

        // 重新计算正确率
        double newRate = ((currentRate * totalPlays) + (isCorrect ? 1 : 0)) / (totalPlays + 1);
        question.setCorrectRate(newRate);
        question.setPlayCount(totalPlays + 1);

        questionRepository.save(question);
    }

    // DTO与Entity转换方法
    private void convertDtoToEntity(QuestionDTO dto, Question entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setMapImageUrl(dto.getMapImageUrl());
        entity.setCorrectLocation(dto.getCorrectLocation());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setBuildingName(dto.getBuildingName());
        entity.setHint(dto.getHint());
        entity.setDifficultyLevel(dto.getDifficultyLevel());
        entity.setCampusId(dto.getCampusId());

        if (dto.getIsApproved() != null) {
            entity.setIsApproved(dto.getIsApproved());
        }
    }

    private QuestionResponse convertToResponseDTO(Question question) {
        QuestionResponse dto = new QuestionResponse();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setDescription(question.getDescription());
        dto.setMapImageUrl(question.getMapImageUrl());
        dto.setBuildingName(question.getBuildingName());
        dto.setHintLevel(question.getHintLevel());
        dto.setHint(question.getHint());
        dto.setDifficultyLevel(question.getDifficultyLevel());
        dto.setCreatorName(question.getCreator().getUsername());
        dto.setPlayCount(question.getPlayCount());
        dto.setCorrectRate(question.getCorrectRate());
        dto.setCreatedAt(question.getCreatedAt());
        return dto;
    }
}