package com.psyche.repository;

import com.psyche.model.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRewardRepository extends JpaRepository<UserReward, Long> {
    List<UserReward> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndRewardKey(Long userId, String rewardKey);
}
