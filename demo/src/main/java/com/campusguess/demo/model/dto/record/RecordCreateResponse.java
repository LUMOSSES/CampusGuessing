package com.campusguess.demo.model.dto.record;

import lombok.Data;

@Data
public class RecordCreateResponse {
    private Long recordId;
    private Integer earnPoints;

    public RecordCreateResponse(Long recordId, Integer earnPoints) {
        this.recordId = recordId;
        this.earnPoints = earnPoints;
    }
}
