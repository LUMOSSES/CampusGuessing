package com.campusguess.demo.controller;

import com.campusguess.demo.model.dto.response.ApiResponse;
import com.campusguess.demo.service.ImageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ImageController {

    private final ImageClient imageClient;

    /**
     * 代理获取图床完整返回（用于调试）
     */
    @GetMapping("/images/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listImages() {
        Map<String, Object> body = imageClient.fetchAllImages();
        if (body == null) {
            return ResponseEntity.ok(ApiResponse.success("图床无返回或不可用", null));
        }
        return ResponseEntity.ok(ApiResponse.success("获取成功", body));
    }
}
