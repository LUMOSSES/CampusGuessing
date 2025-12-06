package com.campusguess.demo.service;

import com.campusguess.demo.model.dto.question.QuestionCreate;
import com.campusguess.demo.model.dto.question.QuestionDTO;
import com.campusguess.demo.model.dto.question.QuestionResponse;
import com.campusguess.demo.model.entity.Question;
import com.campusguess.demo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface QuestionService {

    // 创建问题
    QuestionResponse createQuestion(QuestionCreate dto, User creator);

    // 更新问题
    QuestionResponse updateQuestion(Long id, QuestionDTO dto, User user);

    // 获取问题详情（用户视角，不包含答案）
    QuestionResponse getQuestionById(Long id);

    // 获取问题详情（管理员或创建者视角）
    QuestionDTO getQuestionDetail(Long id, User user);

    // 分页获取问题列表
    Page<QuestionResponse> getQuestions(Pageable pageable, Map<String, Object> filters);

    // 审核问题
    QuestionResponse approveQuestion(Long id, User admin);

    // 删除问题
    void deleteQuestion(Long id, User user);

    // 用户回答问题
    Map<String, Object> answerQuestion(Long questionId, Double userLat, Double userLng, User user);

    // 获取随机问题
    QuestionResponse getRandomQuestion(Integer campusId);

    // 获取用户创建的问题
    Page<QuestionResponse> getUserQuestions(Long userId, Pageable pageable);
}