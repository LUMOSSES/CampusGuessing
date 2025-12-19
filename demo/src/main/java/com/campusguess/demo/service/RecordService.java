package com.campusguess.demo.service;

import com.campusguess.demo.model.dto.record.*;

import java.util.List;

public interface RecordService {
    RecordCreateResponse submitRecord(RecordRequest request);

    List<RecordListItem> getUserRecords(Long userId);

    RecordDetailResponse getRecordDetail(Long userId, Long recordId);
}
