package com.campusguess.demo.model.dto.record;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class RecordRequest {
    @NotNull
    private Long userId;
    @NotNull
        private List<QuestionRecordRequest> questionRecords;

    // Uses QuestionRecordRequest and CoordDTO for inner structures
}