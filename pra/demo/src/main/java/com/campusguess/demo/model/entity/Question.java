package com.campusguess.demo.model.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 问题标题
    private String description; // 问题描述

    @Column(name = "map_image_url")
    private String mapImageUrl; // 校园平面图图片URL

    @Column(name = "correct_location")
    private String correctLocation; // 正确答案位置（经纬度或坐标）

    private Double latitude; // 维度，用于坐标计算
    private Double longitude; // 经度，用于坐标计算

    @Column(name = "building_name")
    private String buildingName; // 建筑名称

    @Column(name = "hint_level")
    private Integer hintLevel = 1; // 提示等级（1-3）

    private String hint; // 提示信息

    @Column(name = "difficulty_level")
    private String difficultyLevel; // 难度等级：EASY, MEDIUM, HARD

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator; // 创建者

    @Column(name = "campus_id")
    private Integer campusId; // 校区ID（珠海校区为1）

    @Column(name = "is_approved")
    private Boolean isApproved = false; // 是否审核通过

    @Column(name = "play_count")
    private Integer playCount = 0; // 被游玩次数

    @Column(name = "correct_rate")
    private Double correctRate = 0.0; // 正确率

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}