package com.campusguess.demo.controller;

import com.campusguess.demo.model.dto.record.*;
import com.campusguess.demo.model.dto.response.ApiResponse;
import com.campusguess.demo.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    // 提交游戏记录
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<RecordCreateResponse>> submitRecord(@Valid @RequestBody RecordRequest request) {
        RecordCreateResponse resp = recordService.submitRecord(request);
        return ResponseEntity.status(201).body(ApiResponse.created("记录提交成功，已计算积分", resp));
    }

    // 获取用户游戏记录列表
    @GetMapping("/users/{userId}/records")
    public ResponseEntity<ApiResponse<List<RecordListItem>>> listUserRecords(@PathVariable Long userId) {
        List<RecordListItem> list = recordService.getUserRecords(userId);
        return ResponseEntity.ok(ApiResponse.success("查询成功", list));
    }

    // 获取单个记录详情
    @GetMapping("/users/{userId}/records/{recordId}")
    public ResponseEntity<ApiResponse<RecordDetailResponse>> getRecordDetail(@PathVariable Long userId, @PathVariable Long recordId) {
        RecordDetailResponse resp = recordService.getRecordDetail(userId, recordId);
        return ResponseEntity.ok(ApiResponse.success("查询单个游戏记录成功", resp));
    }
}