package com.campusguess.demo.model.dto.question;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class QuestionDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "图片URL不能为空")
    private String mapImageUrl;

    @NotBlank(message = "正确位置不能为空")
    private String correctLocation;

    @NotNull(message = "纬度不能为空")
    private Double latitude;

    @NotNull(message = "经度不能为空")
    private Double longitude;

    private String buildingName;
    private String hint;
    private String difficultyLevel;
    private Integer campusId;
    private Boolean isApproved;
}