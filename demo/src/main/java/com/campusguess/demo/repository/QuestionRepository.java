package com.campusguess.demo.repository;

import com.campusguess.demo.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findByAuthorId(Long authorId, Pageable pageable);
    Page<Question> findByAuthorUsername(String username, Pageable pageable);
}
