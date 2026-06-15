package com.psyche.service;

import com.psyche.model.LoginSession;
import com.psyche.model.User;
import com.psyche.model.UserReward;
import com.psyche.repository.LoginSessionRepository;
import com.psyche.repository.UserRewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RewardService {

    @Autowired private UserRewardRepository rewardRepo;
    @Autowired private LoginSessionRepository sessionRepo;
    @Autowired private HabitService habitService;

    // ── Called when user completes ALL tasks for today ─────────────
    public List<UserReward> onAllTasksCompleted(User user) {
        Long uid = user.getId();

        // Find latest login session
        LoginSession session = sessionRepo.findTopByUserIdOrderByLoginAtDesc(uid).orElse(null);

        if (session != null && session.getTasksCompletedAt() == null) {
            // Record completion time
            session.setTasksCompletedAt(LocalDateTime.now());
            long mins = ChronoUnit.MINUTES.between(session.getLoginAt(), LocalDateTime.now());
            session.setMinutesToComplete((int) mins);
            sessionRepo.save(session);

            // Check speed rewards
            if (mins <= 10) grantIfNew(user, "SPEED_10",  "⚡ Speed Demon",      "Completed all tasks within 10 minutes of login!", "#f59e0b");
            if (mins <= 30) grantIfNew(user, "SPEED_30",  "🚀 Quick Achiever",   "Completed all tasks within 30 minutes of login!", "#6366f1");
            if (mins <= 60) grantIfNew(user, "SPEED_60",  "🎯 Same-Hour Hero",   "Completed all tasks within 1 hour of login!",     "#8b5cf6");
        }

        // Streak milestones
        int streak = habitService.calculateGlobalStreak(uid);
        if (streak >= 3)   grantIfNew(user, "STREAK_3",   "🔥 On a Roll",         "3 days in a row — habit forming!",               "#f97316");
        if (streak >= 7)   grantIfNew(user, "STREAK_7",   "⚡ Week Warrior",      "7-day streak — one full week!",                  "#eab308");
        if (streak >= 14)  grantIfNew(user, "STREAK_14",  "🌟 Fortnight Fighter", "14-day streak — two solid weeks!",               "#22c55e");
        if (streak >= 30)  grantIfNew(user, "STREAK_30",  "🏆 Monthly Master",    "30-day streak — a full month of growth!",        "#6366f1");
        if (streak >= 60)  grantIfNew(user, "STREAK_60",  "💎 Diamond Habit",     "60 days straight — elite level consistency!",    "#06b6d4");
        if (streak >= 100) grantIfNew(user, "STREAK_100", "👑 Legend",            "100-day streak — you are LEGENDARY!",            "#a855f7");

        // All-time completions
        int total = habitService.allTimeCompleted(uid);
        if (total >= 1)   grantIfNew(user, "FIRST_TASK",  "🌱 First Step",        "Completed your very first task!",                "#16a34a");
        if (total >= 10)  grantIfNew(user, "TASKS_10",    "💪 Getting Stronger",  "10 tasks completed — habit is forming!",         "#3b82f6");
        if (total >= 25)  grantIfNew(user, "TASKS_25",    "🎖️ Task Veteran",      "25 tasks done — you're serious about growth!",   "#8b5cf6");
        if (total >= 50)  grantIfNew(user, "TASKS_50",    "🥇 Half Century",      "50 total tasks completed — incredible effort!",  "#f59e0b");
        if (total >= 100) grantIfNew(user, "TASKS_100",   "🏅 Century Club",      "100 tasks done — you've transformed yourself!", "#dc2626");

        return rewardRepo.findByUserIdOrderByEarnedAtDesc(uid);
    }

    // ── Grant a reward only if user doesn't already have it ────────
    private void grantIfNew(User user, String key, String title, String desc, String color) {
        if (!rewardRepo.existsByUserIdAndRewardKey(user.getId(), key)) {
            UserReward r = new UserReward();
            r.setUser(user);
            r.setRewardKey(key);
            // Parse "emoji title" format
            int spaceIdx = title.indexOf(' ');
            if (spaceIdx > 0) {
                r.setEmoji(title.substring(0, spaceIdx));
                r.setTitle(title.substring(spaceIdx + 1));
            } else {
                r.setEmoji("🏆");
                r.setTitle(title);
            }
            r.setDescription(desc);
            r.setBadgeColor(color);
            rewardRepo.save(r);
        }
    }

    public List<UserReward> getRewards(Long userId) {
        return rewardRepo.findByUserIdOrderByEarnedAtDesc(userId);
    }

    public int getRewardCount(Long userId) {
        return rewardRepo.findByUserIdOrderByEarnedAtDesc(userId).size();
    }
}
