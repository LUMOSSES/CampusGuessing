package com.campusguess.demo.repository;

import com.campusguess.demo.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 根据校区ID查找问题
    Page<Question> findByCampusId(Integer campusId, Pageable pageable);

    // 根据难度等级查找
    Page<Question> findByDifficultyLevel(String difficultyLevel, Pageable pageable);

    // 查找已审核通过的问题
    Page<Question> findByIsApprovedTrue(Pageable pageable);

    // 根据创建者查找
    Page<Question> findByCreatorId(Long creatorId, Pageable pageable);

    // 查找热门问题（按游玩次数排序）
    Page<Question> findByIsApprovedTrueOrderByPlayCountDesc(Pageable pageable);

    // 随机获取一个问题
    @Query(value = "SELECT * FROM questions WHERE is_approved = true AND campus_id = :campusId ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomQuestion(@Param("campusId") Integer campusId);

    // 根据建筑名称搜索
    List<Question> findByBuildingNameContainingAndIsApprovedTrue(String buildingName);

    // 统计用户创建的问题数量
    Long countByCreatorId(Long creatorId);
}