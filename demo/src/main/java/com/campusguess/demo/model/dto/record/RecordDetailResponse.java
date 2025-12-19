package com.campusguess.demo.model.dto.record;

import lombok.Data;

import java.util.List;

@Data
public class RecordDetailResponse {
    private GameRecordBaseDTO gameRecordBase;
    private List<QuestionDetailDTO> questionDetails;
    private PointChangeDTO pointChange;

    public RecordDetailResponse(GameRecordBaseDTO gameRecordBase, List<QuestionDetailDTO> questionDetails, PointChangeDTO pointChange) {
        this.gameRecordBase = gameRecordBase;
        this.questionDetails = questionDetails;
        this.pointChange = pointChange;
    }
}
