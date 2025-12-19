// src/main/java/com/campusguess/demo/service/FriendService.java
package com.campusguess.demo.service;

import com.campusguess.demo.model.dto.user.*;
import org.springframework.data.domain.Pageable;

public interface FriendService {

    // 2.4 添加好友（发送好友申请）
    FriendshipResponse addFriend(String username, AddFriendRequest request);

    // 2.5 处理好友申请（通过 friendName 定位申请发起者）
    FriendshipResponse handleFriendRequest(String username, String friendName, HandleFriendRequest request);

    // 2.6 获取好友列表
    FriendListResponse getFriendList(String username, Pageable pageable);

    // 获取待处理的好友申请列表
    FriendListResponse getPendingRequests(String username, Pageable pageable);

    // 获取已发送的好友申请列表
    FriendListResponse getSentRequests(String username, Pageable pageable);

    // 检查是否已经是好友
    boolean isFriend(String username, String friendUsername);

    // 删除好友
    void removeFriend(String username, String friendUsername);

    // 取消好友申请
    void cancelFriendRequest(String username, Long friendshipId);

    // 获取好友数量
    Long getFriendCount(String username);

    // 获取好友ID列表（新增）
    java.util.List<Long> getFriendIds(String username);
}