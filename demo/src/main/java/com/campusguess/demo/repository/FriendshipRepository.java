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

        // 查找特定申请（基于用户名）
        Optional<Friendship> findBySenderUsernameAndReceiverUsername(String senderUsername, String receiverUsername);

        // 查找两个用户之间的所有关系（双向）基于用户名
        @Query("SELECT f FROM Friendship f WHERE " +
                        "(f.sender.username = :username1 AND f.receiver.username = :username2) OR " +
                        "(f.sender.username = :username2 AND f.receiver.username = :username1)")
        List<Friendship> findBetweenUsersByUsernames(@Param("username1") String username1, @Param("username2") String username2);

        // 查找用户收到的好友申请（待处理）基于用户名
        @Query("SELECT f FROM Friendship f WHERE " +
                        "f.receiver.username = :username AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.PENDING")
        Page<Friendship> findPendingRequestsByReceiverUsername(@Param("username") String username, Pageable pageable);

        // 查找用户发送的好友申请（待处理）基于用户名
        @Query("SELECT f FROM Friendship f WHERE " +
                        "f.sender.username = :username AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.PENDING")
        Page<Friendship> findPendingRequestsBySenderUsername(@Param("username") String username, Pageable pageable);

        // 查找用户的好友列表（已接受的好友关系）基于用户名
        @Query("SELECT f FROM Friendship f WHERE " +
                        "((f.sender.username = :username OR f.receiver.username = :username) AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED)")
        Page<Friendship> findApprovedFriendshipsByUsername(@Param("username") String username, Pageable pageable);

        // 检查是否已经发送过申请（基于用户名）
        boolean existsBySenderUsernameAndReceiverUsernameAndStatus(String senderUsername, String receiverUsername,
                        Friendship.FriendshipStatus status);

        // 检查是否已经是好友（双向检查，基于用户名）
        @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
                        "((f.sender.username = :username1 AND f.receiver.username = :username2) OR " +
                        "(f.sender.username = :username2 AND f.receiver.username = :username1)) AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED")
        boolean existsFriendshipByUsernames(@Param("username1") String username1, @Param("username2") String username2);

        // 统计好友数量（基于用户名，去重好友 ID）
        @Query("SELECT COUNT(DISTINCT CASE WHEN f.sender.username = :username THEN f.receiver.id ELSE f.sender.id END) FROM Friendship f WHERE " +
                        "(f.sender.username = :username OR f.receiver.username = :username) AND " +
                        "f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED")
        Long countFriendsByUsername(@Param("username") String username);

        // 查找好友 ID 列表（基于用户名，去重）
        @Query("SELECT DISTINCT CASE WHEN f.sender.username = :username THEN f.receiver.id ELSE f.sender.id END " +
                        "FROM Friendship f " +
                        "WHERE (f.sender.username = :username OR f.receiver.username = :username) " +
                        "AND f.status = com.campusguess.demo.model.entity.Friendship.FriendshipStatus.APPROVED")
        List<Long> findFriendIdsByUsername(@Param("username") String username);
}