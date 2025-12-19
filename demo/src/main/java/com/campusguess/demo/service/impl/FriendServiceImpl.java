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
    public List<Long> getFriendIds(String username) {
        // 验证用户存在
        userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 查询好友ID列表（已接受的好友关系）
        return friendshipRepository.findFriendIdsByUsername(username);
    }

    @Override
    @Transactional
    public FriendshipResponse addFriend(String username, AddFriendRequest request) {
        String friendUsername = request.getFriendUsername();

        // 1. 验证不能添加自己为好友
        if (username.equals(friendUsername)) {
            throw new BusinessException(400, "不能添加自己为好友");
        }

        // 2. 验证好友用户是否存在
        User friendUser = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new BusinessException(404, "目标用户不存在"));

        // 3. 检查是否已经发送过申请（当前用户→对方）
        Optional<Friendship> mySentRequestOpt = friendshipRepository.findBySenderUsernameAndReceiverUsername(username, friendUsername);
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
                    log.info("用户{}重新向用户{}发送好友申请，删除旧拒绝记录", username, friendUsername);
                    break;
            }
        }

        // 4. 检查是否已经收到对方申请（对方→当前用户）
        Optional<Friendship> receivedRequestOpt = friendshipRepository.findBySenderUsernameAndReceiverUsername(friendUsername, username);
        if (receivedRequestOpt.isPresent()) {
            Friendship receivedRequest = receivedRequestOpt.get();

            if (receivedRequest.getStatus() == Friendship.FriendshipStatus.PENDING) {
                // 对方已发送申请，直接接受并创建双向关系
                return acceptExistingRequest(username, friendUsername, receivedRequest);
            } else if (receivedRequest.getStatus() == Friendship.FriendshipStatus.APPROVED) {
                throw new BusinessException(400, "你们已经是好友了");
            } else if (receivedRequest.getStatus() == Friendship.FriendshipStatus.REJECTED) {
                // 对方之前发送的申请被当前用户拒绝了
                // 现在当前用户主动申请，应该允许创建新申请
                // 但我们需要检查是否已经有双向好友关系
                if (friendshipRepository.existsFriendshipByUsernames(username, friendUsername)) {
                    throw new BusinessException(400, "你们已经是好友了");
                }
                // 可以继续创建新的申请
            }
        }

        // 5. 检查是否已经是好友（双向检查）
        if (friendshipRepository.existsFriendshipByUsernames(username, friendUsername)) {
            throw new BusinessException(400, "你们已经是好友了");
        }

        // 8. 创建新的好友申请
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Friendship friendship = new Friendship();
        friendship.setSender(user);
        friendship.setReceiver(friendUser);
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);

        Friendship saved = friendshipRepository.save(friendship);

        // 9. 记录日志
        log.info("用户{}向用户{}发送了好友申请，申请ID: {}", username, friendUsername, saved.getId());

        // 10. 返回响应
        return createFriendshipResponse(saved);
    }

    /**
     * 接受已存在的好友申请并创建双向关系
     */
    private FriendshipResponse acceptExistingRequest(String username, String friendUsername, Friendship existingRequest) {
        // 1. 更新对方发来的申请为已接受
        existingRequest.setStatus(Friendship.FriendshipStatus.APPROVED);
        existingRequest.setHandledAt(LocalDateTime.now());
        existingRequest.setHandledType("accept");
        Friendship savedRequest = friendshipRepository.save(existingRequest);

        // 2. 检查是否已经存在反向记录（当前用户→对方）
        Optional<Friendship> reverseRequestOpt = friendshipRepository.findBySenderUsernameAndReceiverUsername(username, friendUsername);

        if (reverseRequestOpt.isPresent()) {
            // 如果存在反向记录，更新它而不是创建新的
            Friendship reverseRequest = reverseRequestOpt.get();
            reverseRequest.setStatus(Friendship.FriendshipStatus.APPROVED);
            reverseRequest.setHandledAt(LocalDateTime.now());
            reverseRequest.setHandledType("accept");
            friendshipRepository.save(reverseRequest);
            log.info("用户{}接受了用户{}的好友申请，并更新了已有的反向记录", username, friendUsername);
        } else {
            // 创建反向的好友关系记录
            Friendship reverseFriendship = new Friendship();
            reverseFriendship.setSender(existingRequest.getReceiver()); // 当前用户
            reverseFriendship.setReceiver(existingRequest.getSender()); // 对方用户
            reverseFriendship.setStatus(Friendship.FriendshipStatus.APPROVED);
            reverseFriendship.setHandledAt(LocalDateTime.now());
            reverseFriendship.setHandledType("accept");
            friendshipRepository.save(reverseFriendship);
            log.info("用户{}接受了用户{}的好友申请，并创建了新的反向记录", username, friendUsername);
        }

        log.info("用户{}自动接受了用户{}的好友申请，建立了双向好友关系", username, friendUsername);

        // 返回已接受的申请响应
        return createFriendshipResponse(savedRequest);
    }

    public FriendshipResponse handleFriendRequest(String username, String friendName, HandleFriendRequest request) {
        // 1. 根据发起者和接收者用户名查找好友申请（发起者 = friendName, 接收者 = username）
        Friendship friendship = friendshipRepository
                .findBySenderUsernameAndReceiverUsername(friendName, username)
                .orElseThrow(() -> new BusinessException(404, "好友申请不存在"));

        // 2. 验证当前用户是否为接收者（基于用户名）
        if (!friendship.getReceiver().getUsername().equals(username)) {
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
            String senderUsername = friendship.getSender().getUsername();
            String receiverUsername = friendship.getReceiver().getUsername();

            Optional<Friendship> reverseRequestOpt = friendshipRepository
                    .findBySenderUsernameAndReceiverUsername(receiverUsername, senderUsername);

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
                    username, friendship.getSender().getId(), friendship.getId());
        } else if ("reject".equals(handleType)) {
            // 拒绝申请：改为直接调用删除好友关系（删除所有两者之间的关系），而不是把状态改为 REJECTED
            String senderUsername = friendship.getSender().getUsername();
            // 调用删除接口（同一 service 内方法），会删除两者之间的所有关系（包括当前的 pending 申请）
            removeFriend(username, senderUsername);

            // 为了返回一致的响应，设置处理信息但不保存到数据库（记录已被删除）
            friendship.setHandledType("reject");
            friendship.setHandledAt(now);

            log.info("用户{}拒绝了用户{}的好友申请（通过删除关系实现）",
                    username, friendship.getSender().getId());

            // 直接返回响应（不再保存 friendship，因为已被删除）
            return createFriendshipResponse(friendship);
        } else {
            throw new BusinessException(400, "处理类型必须是accept或reject");
        }

        Friendship updated = friendshipRepository.save(friendship);

        // 5. 返回响应
        return createFriendshipResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getFriendList(String username, Pageable pageable) {
        // 验证用户存在
        userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 使用去重的 friendId 列表来避免重复（数据库中可能存在双向两条记录）
        List<Long> friendIds = friendshipRepository.findFriendIdsByUsername(username);

        // 总数为去重后的好友数量
        long total = friendIds.size();

        // 根据 pageable 在内存中做分页（因为 repository 返回的实体可能包含重复记录）
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int start = page * size;
        int end = Math.min(start + size, friendIds.size());

        List<FriendResponse> friendList;
        if (start >= end) {
            friendList = List.of();
        } else {
            List<Long> pageIds = friendIds.subList(start, end);

            // 逐项查找用户并构造响应，保持顺序
            friendList = pageIds.stream()
                .map(id -> userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "好友用户不存在")))
                .map(friendUser -> new FriendResponse(
                    friendUser.getId(),
                    friendUser.getUsername(),
                    friendUser.getPoints(),
                    Friendship.FriendshipStatus.APPROVED.name().toLowerCase(),
                    friendUser.getLastLoginAt(),
                    null))
                .toList();
        }

        log.debug("用户{}查询好友列表（去重），共{}条记录", username, total);

        return new FriendListResponse(total, friendList);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getPendingRequests(String username, Pageable pageable) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Page<Friendship> pendingRequests = friendshipRepository.findPendingRequestsByReceiverUsername(username, pageable);

        List<FriendResponse> requestList = pendingRequests.stream()
                .map(friendship -> new FriendResponse(
                        friendship.getSender().getId(),
                        friendship.getSender().getUsername(),
                        friendship.getSender().getPoints(),
                        friendship.getStatus().name().toLowerCase(),
                        friendship.getSender().getLastLoginAt(),
                        friendship.getRequestedAt()))
                .toList();

        log.debug("用户{}查询待处理好友申请，共{}条记录", username, pendingRequests.getTotalElements());

        return new FriendListResponse(pendingRequests.getTotalElements(), requestList);
    }

    @Override
    @Transactional(readOnly = true)
    public FriendListResponse getSentRequests(String username, Pageable pageable) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Page<Friendship> sentRequests = friendshipRepository.findPendingRequestsBySenderUsername(username, pageable);

        List<FriendResponse> requestList = sentRequests.stream()
                .map(friendship -> new FriendResponse(
                        friendship.getReceiver().getId(),
                        friendship.getReceiver().getUsername(),
                        friendship.getReceiver().getPoints(),
                        friendship.getStatus().name().toLowerCase(),
                        friendship.getReceiver().getLastLoginAt(),
                        friendship.getRequestedAt()))
                .toList();

        log.debug("用户{}查询已发送的好友申请，共{}条记录", username, sentRequests.getTotalElements());

        return new FriendListResponse(sentRequests.getTotalElements(), requestList);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFriend(String username, String friendUsername) {
        boolean isFriend = friendshipRepository.existsFriendshipByUsernames(username, friendUsername);
        log.debug("检查用户{}和用户{}是否是好友：{}", username, friendUsername, isFriend);
        return isFriend;
    }

    @Override
    @Transactional
    public void removeFriend(String username, String friendUsername) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new BusinessException(404, "好友用户不存在"));

        List<Friendship> friendships = friendshipRepository.findBetweenUsersByUsernames(username, friendUsername);

        if (friendships.isEmpty()) {
            throw new BusinessException(404, "好友关系不存在");
        }

        friendshipRepository.deleteAll(friendships);
        log.info("用户{}删除了好友{}，删除了{}条关系记录", username, friendUsername, friendships.size());
    }

    @Override
    @Transactional
    public void cancelFriendRequest(String username, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(404, "好友申请不存在"));

        if (!friendship.getSender().getUsername().equals(username)) {
            throw new BusinessException(403, "只能取消自己发送的好友申请");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new BusinessException(400, "只能取消待处理的好友申请");
        }

        friendshipRepository.delete(friendship);
        log.info("用户{}取消了好友申请ID: {}", username, friendshipId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFriendCount(String username) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Long count = friendshipRepository.countFriendsByUsername(username);
        log.debug("用户{}的好友数量：{}", username, count);
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