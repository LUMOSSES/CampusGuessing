package com.campusguess.demo.controller;

import com.campusguess.demo.exception.BusinessException;
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
@RequestMapping("/users/{userId}/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 2.4 添加好友
     * POST /users/{userId}/friends
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FriendshipResponse>> addFriend(
            @PathVariable Long userId,
            @Valid @RequestBody AddFriendRequest addFriendRequest) {

        try {
            // 直接使用路径参数中的userId，不需要从JWT获取
            FriendshipResponse response = friendService.addFriend(userId, addFriendRequest);
            return ResponseEntity.ok(ApiResponse.success("好友添加成功，等待对方确认", response));
        } catch (BusinessException e) {
            if (e.getCode() == 200) {
                return ResponseEntity.ok(ApiResponse.success(e.getMessage(), null));
            }
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 2.5 处理好友申请
     * PUT /users/{userId}/friends/applications/{friendshipId}
     */
    @PutMapping("/applications/{friendshipId}")
    public ResponseEntity<ApiResponse<FriendshipResponse>> handleFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long friendshipId,
            @Valid @RequestBody HandleFriendRequest handleFriendRequest) {

        try {
            // 直接使用路径参数中的userId
            FriendshipResponse response = friendService.handleFriendRequest(userId, friendshipId, handleFriendRequest);

            String message = "accept".equalsIgnoreCase(handleFriendRequest.getHandleType())
                    ? "好友申请接受成功，已添加为好友"
                    : "好友申请拒绝成功";

            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 2.6 获取好友列表
     * GET /users/{userId}/friends
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FriendListResponse>> getFriendList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            // 直接使用路径参数中的userId
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "handledAt"));
            FriendListResponse response = friendService.getFriendList(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success("好友列表查询成功", response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 获取待处理的好友申请列表
     * GET /users/{userId}/friends/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<FriendListResponse>> getPendingRequests(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            // 直接使用路径参数中的userId
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
            FriendListResponse response = friendService.getPendingRequests(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success("待处理好友申请查询成功", response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 获取已发送的好友申请列表
     * GET /users/{userId}/friends/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<FriendListResponse>> getSentRequests(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            // 直接使用路径参数中的userId
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
            FriendListResponse response = friendService.getSentRequests(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success("已发送的好友申请查询成功", response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 删除好友
     * DELETE /users/{userId}/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {

        try {
            // 直接使用路径参数中的userId
            friendService.removeFriend(userId, friendId);
            return ResponseEntity.ok(ApiResponse.success("好友删除成功", null));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 检查是否已经是好友
     * GET /users/{userId}/friends/check/{friendId}
     */
    @GetMapping("/check/{friendId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendship(
            @PathVariable Long userId,
            @PathVariable Long friendId) {

        try {
            // 直接使用路径参数中的userId
            boolean isFriend = friendService.isFriend(userId, friendId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", isFriend));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 取消好友申请
     * DELETE /users/{userId}/friends/applications/{friendshipId}
     */
    @DeleteMapping("/applications/{friendshipId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long friendshipId) {

        try {
            // 直接使用路径参数中的userId
            friendService.cancelFriendRequest(userId, friendshipId);
            return ResponseEntity.ok(ApiResponse.success("好友申请取消成功", null));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }

    /**
     * 获取好友数量
     * GET /users/{userId}/friends/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFriendCount(
            @PathVariable Long userId) {

        try {
            // 直接使用路径参数中的userId
            Long count = friendService.getFriendCount(userId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", count));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "服务器异常: " + e.getMessage()));
        }
    }
}