package com.campusguess.demo.repository;

import com.campusguess.demo.model.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

        // 查找特定申请
        Optional<Friendship> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

        // 查找两个用户之间的所有关系（双向）
        @Query("SELECT f FROM Friendship f WHERE " +
                        "(f.sender.id = :userId1 AND f.receiver.id = :userId2) OR " +
                        "(f.sender.id = :userId2 AND f.receiver.id = :userId1)")
        List<Friendship> findBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

        // 查找用户收到的好友申请（待处理）
        @Query("SELECT f FROM Friendship f WHERE " +
                        "f.receiver.id = :userId AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.PENDING")
        Page<Friendship> findPendingRequestsByReceiverId(@Param("userId") Long userId, Pageable pageable);

        // 查找用户发送的好友申请（待处理）
        @Query("SELECT f FROM Friendship f WHERE " +
                        "f.sender.id = :userId AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.PENDING")
        Page<Friendship> findPendingRequestsBySenderId(@Param("userId") Long userId, Pageable pageable);

        // 查找用户的好友列表（已接受的好友关系）
        @Query("SELECT f FROM Friendship f WHERE " +
                        "((f.sender.id = :userId OR f.receiver.id = :userId) AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED)")
        Page<Friendship> findApprovedFriendships(@Param("userId") Long userId, Pageable pageable);

        // 检查是否已经发送过申请
        boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId,
                        Friendship.FriendshipStatus status);

        // 检查是否已经是好友（双向检查）
        @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
                        "((f.sender.id = :userId1 AND f.receiver.id = :userId2) OR " +
                        "(f.sender.id = :userId2 AND f.receiver.id = :userId1)) AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED")
        boolean existsFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

        // 统计好友数量
        @Query("SELECT COUNT(f) FROM Friendship f WHERE " +
                        "(f.sender.id = :userId OR f.receiver.id = :userId) AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED")
        Long countFriends(@Param("userId") Long userId);

        @Query("SELECT CASE WHEN f.sender.id = :userId THEN f.receiver.id ELSE f.sender.id END " +
                        "FROM Friendship f " +
                        "WHERE (f.sender.id = :userId OR f.receiver.id = :userId) " +
                        "AND f.status = 'APPROVED'")
        List<Long> findFriendIdsByUserId(Long userId);
}