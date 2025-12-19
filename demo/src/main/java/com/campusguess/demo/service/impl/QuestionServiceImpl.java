package com.campusguess.demo.service.impl;

import com.campusguess.demo.exception.BusinessException;
import com.campusguess.demo.model.dto.question.CreateQuestionRequest;
import com.campusguess.demo.model.dto.question.QuestionListResponse;
import com.campusguess.demo.model.dto.question.QuestionResponse;
import com.campusguess.demo.model.entity.Question;
import com.campusguess.demo.model.entity.User;
import com.campusguess.demo.repository.QuestionRepository;
import com.campusguess.demo.repository.UserRepository;
import com.campusguess.demo.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final com.campusguess.demo.service.ImageClient imageClient;

    @Override
    @Transactional
    public QuestionResponse createQuestion(String username, CreateQuestionRequest request) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Question q = new Question();
        q.setTitle(request.getTitle());
        q.setContent(request.getContent());
        q.setAnswer(request.getAnswer());
        q.setCampus(request.getCampus());
        q.setDifficulty(request.getDifficulty());
        q.setImageKey(request.getKey());
        if (request.getCorrectCoord() != null) {
            q.setCorrectLon(request.getCorrectCoord().getLon());
            q.setCorrectLat(request.getCorrectCoord().getLat());
        }
        q.setAuthor(author);

        Question saved = questionRepository.save(q);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));
        QuestionResponse resp = toResponse(q);
        // 从图床获取 imageData
        if (q.getImageKey() != null) {
            Map<String, Object> imageData = imageClient.fetchImageByKey(q.getImageKey());
            if (imageData == null) {
                log.warn("未能从图床获取到 imageData，key={}", q.getImageKey());
            } else {
                log.debug("成功获取 imageData，key={}", q.getImageKey());
            }
            resp.setImageData(imageData);
        }
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionListResponse listQuestions(Pageable pageable) {
        Page<Question> page = questionRepository.findAll(pageable);
        List<QuestionResponse> list = page.stream().map(this::toResponse).toList();
        return new QuestionListResponse(page.getTotalElements(), list);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionListResponse listByUser(String username, Pageable pageable) {
        userRepository.findByUsername(username).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        Page<Question> page = questionRepository.findByAuthorUsername(username, pageable);
        List<QuestionResponse> list = page.stream().map(this::toResponse).toList();
        return new QuestionListResponse(page.getTotalElements(), list);
    }

    @Override
    @Transactional
    public void deleteQuestion(String username, Long questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));

        if (q.getAuthor() == null || !q.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(403, "无权删除该题目");
        }

        questionRepository.delete(q);
        log.info("用户{}删除了题目{}", username, questionId);
    }

    private QuestionResponse toResponse(Question q) {
        QuestionResponse r = new QuestionResponse();
        r.setId(q.getId());
        r.setTitle(q.getTitle());
        r.setContent(q.getContent());
        r.setAnswer(q.getAnswer());
        if (q.getAuthor() != null) {
            r.setAuthorId(q.getAuthor().getId());
            r.setAuthorUsername(q.getAuthor().getUsername());
        }
        r.setCreatedAt(q.getCreatedAt());
        r.setCampus(q.getCampus());
        r.setDifficulty(q.getDifficulty());
        if (q.getCorrectLon() != null && q.getCorrectLat() != null) {
            QuestionResponse.CorrectCoord cc = new QuestionResponse.CorrectCoord();
            cc.setLon(q.getCorrectLon());
            cc.setLat(q.getCorrectLat());
            r.setCorrectCoord(cc);
        }
        return r;
    }
}
