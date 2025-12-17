// src/main/java/com/campusguess/demo/service/FriendService.java
package com.campusguess.demo.service;

import com.campusguess.demo.model.dto.user.*;
import org.springframework.data.domain.Pageable;

public interface FriendService {

    // 2.4 添加好友（发送好友申请）
    FriendshipResponse addFriend(Long userId, AddFriendRequest request);

    // 2.5 处理好友申请
    FriendshipResponse handleFriendRequest(Long userId, Long friendshipId, HandleFriendRequest request);

    // 2.6 获取好友列表
    FriendListResponse getFriendList(Long userId, Pageable pageable);

    // 获取待处理的好友申请列表
    FriendListResponse getPendingRequests(Long userId, Pageable pageable);

    // 获取已发送的好友申请列表
    FriendListResponse getSentRequests(Long userId, Pageable pageable);

    // 检查是否已经是好友
    boolean isFriend(Long userId, Long friendId);

    // 删除好友
    void removeFriend(Long userId, Long friendId);

    // 取消好友申请
    void cancelFriendRequest(Long userId, Long friendshipId);

    // 获取好友数量
    Long getFriendCount(Long userId);

    // 获取好友ID列表（新增）
    java.util.List<Long> getFriendIds(Long userId);
}