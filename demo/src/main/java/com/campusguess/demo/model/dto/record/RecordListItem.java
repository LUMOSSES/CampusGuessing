package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class RecordListItem {
    private Long recordId;
    private Integer earnPoints;
    private String createdAt; // ISO

    public RecordListItem(Long recordId, Integer earnPoints, String createdAt) {
        this.recordId = recordId;
        this.earnPoints = earnPoints;
        this.createdAt = createdAt;
    }
}
