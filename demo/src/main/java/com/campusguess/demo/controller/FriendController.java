package com.campusguess.demo.controller;

import com.campusguess.demo.model.dto.user.*;
import com.campusguess.demo.model.dto.response.ApiResponse;
import com.campusguess.demo.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/{username}/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 添加好友 */
    @PostMapping
    public ResponseEntity<ApiResponse<FriendshipResponse>> addFriend(
            @PathVariable String username,
            @Valid @RequestBody AddFriendRequest request) {
        FriendshipResponse response = friendService.addFriend(username, request);
        return ResponseEntity.ok(ApiResponse.success("好友添加成功，等待对方确认", response));
    }

    /** 处理好友申请 */
    @PutMapping("/applications")
    public ResponseEntity<ApiResponse<FriendshipResponse>> handleFriendRequest(
            @PathVariable String username,
            @Valid @RequestBody HandleFriendRequest request) {
        FriendshipResponse response = friendService.handleFriendRequest(
                username, request.getFriendUsername(), request);
        String message = "accept".equalsIgnoreCase(request.getHandleType())
                ? "好友申请接受成功" : "好友申请拒绝成功";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /** 获取好友列表 */
    @GetMapping
    public ResponseEntity<ApiResponse<FriendListResponse>> getFriendList(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "handledAt"));
        FriendListResponse response = friendService.getFriendList(username, pageable);
        return ResponseEntity.ok(ApiResponse.success("好友列表查询成功", response));
    }

    /** 获取待处理的好友申请 */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<FriendListResponse>> getPendingRequests(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        FriendListResponse response = friendService.getPendingRequests(username, pageable);
        return ResponseEntity.ok(ApiResponse.success("待处理好友申请查询成功", response));
    }

    /** 获取已发送的好友申请 */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<FriendListResponse>> getSentRequests(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        FriendListResponse response = friendService.getSentRequests(username, pageable);
        return ResponseEntity.ok(ApiResponse.success("已发送的好友申请查询成功", response));
    }

    /** 删除好友 */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @PathVariable String username,
            @Valid @RequestBody DeleteFriendRequest request) {
        friendService.removeFriend(username, request.getFriendUsername());
        return ResponseEntity.ok(ApiResponse.success("好友删除成功", null));
    }

    /** 检查是否已是好友 */
    @GetMapping("/check/{friendUsername}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendship(
            @PathVariable String username,
            @PathVariable String friendUsername) {
        boolean isFriend = friendService.isFriend(username, friendUsername);
        return ResponseEntity.ok(ApiResponse.success("查询成功", isFriend));
    }

    /** 取消好友申请 */
    @DeleteMapping("/applications/{friendshipId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @PathVariable String username,
            @PathVariable Long friendshipId) {
        friendService.cancelFriendRequest(username, friendshipId);
        return ResponseEntity.ok(ApiResponse.success("好友申请取消成功", null));
    }

    /** 获取好友数量 */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFriendCount(@PathVariable String username) {
        Long count = friendService.getFriendCount(username);
        return ResponseEntity.ok(ApiResponse.success("查询成功", count));
    }
}
