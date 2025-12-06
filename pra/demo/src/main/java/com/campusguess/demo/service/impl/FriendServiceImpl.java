package com.campusguess.demo.service.impl;

import com.campusguess.demo.exception.BusinessException;
import com.campusguess.demo.model.dto.user.*;
import com.campusguess.demo.model.entity.Friendship;
import com.campusguess.demo.model.entity.User;
import com.campusguess.demo.repository.FriendshipRepository;
import com.campusguess.demo.repository.UserRepository;
import com.campusguess.demo.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> getFriendIds(Long userId) {
        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 查询好友ID列表（已接受的好友关系）
        return friendshipRepository.findFriendIdsByUserId(userId);
    }

    @Override
    @Transactional
    public FriendshipResponse addFriend(Long userId, AddFriendRequest request) {
        Long friendId = request.getFriendUserId();

        // 1. 验证不能添加自己为好友
        if (userId.equals(friendId)) {
            throw new BusinessException(400, "不能添加自己为好友");
        }

        // 2. 验证好友用户是否存在
        User friendUser = userRepository.findById(friendId)
                .orElseThrow(() -> new BusinessException(404, "目标用户不存在"));

        // 3. 检查是否已经发送过申请（当前用户→对方）
        Optional<Friendship> mySentRequestOpt = friendshipRepository.findBySenderIdAndReceiverId(userId, friendId);
        if (mySentRequestOpt.isPresent()) {
            Friendship mySentRequest = mySentRequestOpt.get();
            switch (mySentRequest.getStatus()) {
                case PENDING:
                    throw new BusinessException(400, "已向该用户发送过好友申请，等待对方处理");
                case APPROVED:
                    throw new BusinessException(400, "你们已经是好友了");
                case REJECTED:
                    // 如果之前被拒绝，现在重新发送申请
                    // 删除旧的拒绝记录，创建新的申请
                    friendshipRepository.delete(mySentRequest);
                    log.info("用户{}重新向用户{}发送好友申请，删除旧拒绝记录", userId, friendId);
                    break;
            }
        }

        // 4. 检查是否已经收到对方申请（对方→当前用户）
        Optional<Friendship> receivedRequestOpt = friendshipRepository.findBySenderIdAndReceiverId(friendId, userId);
        if (receivedRequestOpt.isPresent()) {
            Friendship receivedRequest = receivedRequestOpt.get();

            if (receivedRequest.getStatus() == Friendship.FriendshipStatus.PENDING) {
                // 对方已发送申请，直接接受并创建双向关系
                return acceptExistingRequest(userId, friendId, receivedRequest);
            } else if (receivedRequest.getStatus() == Friendship.FriendshipStatus.APPROVED) {
                throw new BusinessException(400, "你们已经是好友了");
            } else if (receivedRequest.getStatus() == Friendship.FriendshipStatus.REJECTED) {
                // 对方之前发送的申请被当前用户拒绝了
                // 现在当前用户主动申请，应该允许创建新申请
                // 但我们需要检查是否已经有双向好友关系
                if (friendshipRepository.existsFriendship(userId, friendId)) {
                    throw new BusinessException(400, "你们已经是好友了");
                }
                // 可以继续创建新的申请
            }
        }

        // 5. 检查是否已经是好友（双向检查）
        if (friendshipRepository.existsFriendship(userId, friendId)) {
            throw new BusinessException(400, "你们已经是好友了");
        }

        // 6. 检查对方是否有拒绝过的申请（对方→当前用户），如果有，需要特殊处理
        // 这个逻辑已经在第4步处理了，所以这里不需要重复处理

        // 7. 检查当前用户是否有拒绝过的申请（当前用户→对方），如果有，需要特殊处理
        // 这个逻辑已经在第3步处理了，所以这里不需要重复处理

        // 8. 创建新的好友申请
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Friendship friendship = new Friendship();
        friendship.setSender(user);
        friendship.setReceiver(friendUser);
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);

        Friendship saved = friendshipRepository.save(friendship);

        // 9. 记录日志
        log.info("用户{}向用户{}发送了好友申请，申请ID: {}", userId, friendId, saved.getId());

        // 10. 返回响应
        return createFriendshipResponse(saved);
    }

    /**
     * 接受已存在的好友申请并创建双向关系
     */
    private FriendshipResponse acceptExistingRequest(Long userId, Long friendId, Friendship existingRequest) {
        // 1. 更新对方发来的申请为已接受
        existingRequest.setStatus(Friendship.FriendshipStatus.APPROVED);
        existingRequest.setHandledAt(LocalDateTime.now());
        existingRequest.setHandledType("accept");
        Friendship savedRequest = friendshipRepository.save(existingRequest);

        // 2. 检查是否已经存在反向记录（当前用户→对方）
        Optional<Friendship> reverseRequestOpt = friendshipRepository.findBySenderIdAndReceiverId(userId, friendId);

        if (reverseRequestOpt.isPresent()) {
            // 如果存在反向记录，更新它而不是创建新的
            Friendship reverseRequest = reverseRequestOpt.get();
            reverseRequest.setStatus(Friendship.FriendshipStatus.APPROVED);
            reverseRequest.setHandledAt(LocalDateTime.now());
            reverseRequest.setHandledType("accept");
            friendshipRepository.save(reverseRequest);
            log.info("用户{}接受了用户{}的好友申请，并更新了已有的反向记录", userId, friendId);
        } else {
            // 创建反向的好友关系记录
            Friendship reverseFriendship = new Friendship();
            reverseFriendship.setSender(existingRequest.getReceiver()); // 当前用户
            reverseFriendship.setReceiver(existingRequest.getSender()); // 对方用户
            reverseFriendship.setStatus(Friendship.FriendshipStatus.APPROVED);
            reverseFriendship.setHandledAt(LocalDateTime.now());
            reverseFriendship.setHandledType("accept");
            friendshipRepository.save(reverseFriendship);
            log.info("用户{}接受了用户{}的好友申请，并创建了新的反向记录", userId, friendId);
        }

        log.info("用户{}自动接受了用户{}的好友申请，建立了双向好友关系", userId, friendId);

        // 返回已接受的申请响应
        return createFriendshipResponse(savedRequest);
    }

    public FriendshipResponse handleFriendRequest(Long userId, Long friendshipId, HandleFriendRequest request) {
        // 1. 查找好友申请
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(404, "好友申请不存在"));

        // 2. 验证当前用户是否为接收者
        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new BusinessException(403, "无权处理该好友申请");
        }

        // 3. 验证申请状态是否为待处理
        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new BusinessException(400, "该好友申请已处理");
        }

        // 4. 处理申请
        String handleType = request.getHandleType().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        if ("accept".equals(handleType)) {
            friendship.setStatus(Friendship.FriendshipStatus.APPROVED);
            friendship.setHandledType("accept");
            friendship.setHandledAt(now);

            // 检查是否已经存在反向记录
            Long senderId = friendship.getSender().getId();
            Long receiverId = friendship.getReceiver().getId();

            Optional<Friendship> reverseRequestOpt = friendshipRepository
                    .findBySenderIdAndReceiverId(receiverId, senderId);

            if (reverseRequestOpt.isPresent()) {
                // 如果存在反向记录，更新它
                Friendship reverseRequest = reverseRequestOpt.get();
                reverseRequest.setStatus(Friendship.FriendshipStatus.APPROVED);
                reverseRequest.setHandledAt(now);
                reverseRequest.setHandledType("accept");
                friendshipRepository.save(reverseRequest);
                log.info("接受好友申请时更新了已有的反向记录");
            } else {
                // 创建反向的好友关系记录
                createReverseFriendship(friendship);
            }

            log.info("用户{}接受了用户{}的好友申请，申请ID: {}",
                    userId, friendship.getSender().getId(), friendshipId);
        } else if ("reject".equals(handleType)) {
            friendship.setStatus(Friendship.FriendshipStatus.REJECTED);
            friendship.setHandledType("reject");
            friendship.setHandledAt(now);
            log.info("用户{}拒绝了用户{}的好友申请，申请ID: {}",
                    userId, friendship.getSender().getId(), friendshipId);
        } else {
            throw new BusinessException(400, "处理类型必须是accept或reject");
        }

        Friendship updated = friendshipRepository.save(friendship);

        // 5. 返回响应
        return createFriendshipResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getFriendList(Long userId, Pageable pageable) {
        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 查询好友列表（已接受的好友关系）
        Page<Friendship> friendships = friendshipRepository.findApprovedFriendships(userId, pageable);

        // 转换为响应对象
        List<FriendResponse> friendList = friendships.stream()
                .map(friendship -> {
                    // 确定哪个是好友用户
                    User friendUser = friendship.getSender().getId().equals(userId)
                            ? friendship.getReceiver()
                            : friendship.getSender();

                    return new FriendResponse(
                            friendUser.getId(),
                            friendUser.getUsername(),
                            friendUser.getPoints(),
                            friendship.getStatus().name().toLowerCase(),
                            friendUser.getLastLoginAt(),
                            friendship.getHandledAt());
                })
                .toList();

        log.debug("用户{}查询好友列表，共{}条记录", userId, friendships.getTotalElements());

        return new FriendListResponse(friendships.getTotalElements(), friendList);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getPendingRequests(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Page<Friendship> pendingRequests = friendshipRepository.findPendingRequestsByReceiverId(userId, pageable);

        List<FriendResponse> requestList = pendingRequests.stream()
                .map(friendship -> new FriendResponse(
                        friendship.getSender().getId(),
                        friendship.getSender().getUsername(),
                        friendship.getSender().getPoints(),
                        friendship.getStatus().name().toLowerCase(),
                        friendship.getSender().getLastLoginAt(),
                        friendship.getRequestedAt()))
                .toList();

        log.debug("用户{}查询待处理好友申请，共{}条记录", userId, pendingRequests.getTotalElements());

        return new FriendListResponse(pendingRequests.getTotalElements(), requestList);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getSentRequests(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Page<Friendship> sentRequests = friendshipRepository.findPendingRequestsBySenderId(userId, pageable);

        List<FriendResponse> requestList = sentRequests.stream()
                .map(friendship -> new FriendResponse(
                        friendship.getReceiver().getId(),
                        friendship.getReceiver().getUsername(),
                        friendship.getReceiver().getPoints(),
                        friendship.getStatus().name().toLowerCase(),
                        friendship.getReceiver().getLastLoginAt(),
                        friendship.getRequestedAt()))
                .toList();

        log.debug("用户{}查询已发送的好友申请，共{}条记录", userId, sentRequests.getTotalElements());

        return new FriendListResponse(sentRequests.getTotalElements(), requestList);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFriend(Long userId, Long friendId) {
        boolean isFriend = friendshipRepository.existsFriendship(userId, friendId);
        log.debug("检查用户{}和用户{}是否是好友：{}", userId, friendId, isFriend);
        return isFriend;
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        userRepository.findById(friendId)
                .orElseThrow(() -> new BusinessException(404, "好友用户不存在"));

        List<Friendship> friendships = friendshipRepository.findBetweenUsers(userId, friendId);

        if (friendships.isEmpty()) {
            throw new BusinessException(404, "好友关系不存在");
        }

        friendshipRepository.deleteAll(friendships);
        log.info("用户{}删除了好友{}，删除了{}条关系记录", userId, friendId, friendships.size());
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(404, "好友申请不存在"));

        if (!friendship.getSender().getId().equals(userId)) {
            throw new BusinessException(403, "只能取消自己发送的好友申请");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new BusinessException(400, "只能取消待处理的好友申请");
        }

        friendshipRepository.delete(friendship);
        log.info("用户{}取消了好友申请ID: {}", userId, friendshipId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFriendCount(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Long count = friendshipRepository.countFriends(userId);
        log.debug("用户{}的好友数量：{}", userId, count);
        return count;
    }

    // 辅助方法：创建好友申请响应
    private FriendshipResponse createFriendshipResponse(Friendship friendship) {
        FriendshipResponse response = new FriendshipResponse();
        response.setFriendshipId(friendship.getId());
        response.setApplicantId(friendship.getSender().getId());
        response.setApplicantUsername(friendship.getSender().getUsername());
        response.setReceiverId(friendship.getReceiver().getId());
        response.setReceiverUsername(friendship.getReceiver().getUsername());
        response.setStatus(friendship.getStatus().name().toLowerCase());
        response.setRequestedAt(friendship.getRequestedAt());

        if (friendship.getHandledAt() != null) {
            response.setHandledAt(friendship.getHandledAt());
            response.setHandledType(friendship.getHandledType());
        }

        return response;
    }

    private void createReverseFriendship(Friendship originalFriendship) {
        // 创建反向的好友关系记录（双向好友关系）
        Friendship reverseFriendship = new Friendship();
        reverseFriendship.setSender(originalFriendship.getReceiver());
        reverseFriendship.setReceiver(originalFriendship.getSender());
        reverseFriendship.setStatus(Friendship.FriendshipStatus.APPROVED);
        reverseFriendship.setHandledAt(LocalDateTime.now());
        reverseFriendship.setHandledType("accept");

        friendshipRepository.save(reverseFriendship);

        log.info("创建反向好友关系：用户{}到用户{}",
                originalFriendship.getReceiver().getId(),
                originalFriendship.getSender().getId());
    }
}