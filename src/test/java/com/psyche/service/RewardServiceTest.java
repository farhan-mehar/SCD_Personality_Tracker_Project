package com.psyche.service;

import com.psyche.model.LoginSession;
import com.psyche.model.User;
import com.psyche.model.UserReward;
import com.psyche.repository.LoginSessionRepository;
import com.psyche.repository.UserRewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock private UserRewardRepository rewardRepo;
    @Mock private LoginSessionRepository sessionRepo;
    @Mock private HabitService habitService;
    @InjectMocks private RewardService rewardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
    }

    // ── onAllTasksCompleted: no session ────────────────────────────
    @Test
    void onAllTasksCompleted_noSession_grantsStreakAndTotalRewards() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(1);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "FIRST_TASK")).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        List<UserReward> result = rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(any(UserReward.class));
        assertThat(result).isEmpty();
    }

    // ── onAllTasksCompleted: session already completed ─────────────
    @Test
    void onAllTasksCompleted_sessionAlreadyCompleted_skipsSpeedRewards() {
        LoginSession session = new LoginSession();
        session.setUserId(1L);
        session.setLoginAt(LocalDateTime.now().minusMinutes(5));
        session.setTasksCompletedAt(LocalDateTime.now()); // already set
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.of(session));
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        // No speed rewards saved (session already completed)
        verify(rewardRepo, never()).save(argThat(r ->
            r.getRewardKey() != null && r.getRewardKey().startsWith("SPEED")));
    }

    // ── Speed reward: within 10 mins ──────────────────────────────
    @Test
    void onAllTasksCompleted_within10Mins_grantsSpeedDemon() {
        LoginSession session = new LoginSession();
        session.setUserId(1L);
        session.setLoginAt(LocalDateTime.now().minusMinutes(8));
        session.setTasksCompletedAt(null);
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.of(session));
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "SPEED_10")).thenReturn(false);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "SPEED_30")).thenReturn(false);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "SPEED_60")).thenReturn(false);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo, atLeastOnce()).save(any(UserReward.class));
        verify(sessionRepo).save(session);
    }

    // ── Speed reward: within 30 mins ──────────────────────────────
    @Test
    void onAllTasksCompleted_within30Mins_grantsQuickAchiever() {
        LoginSession session = new LoginSession();
        session.setUserId(1L);
        session.setLoginAt(LocalDateTime.now().minusMinutes(20));
        session.setTasksCompletedAt(null);
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.of(session));
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo, atLeastOnce()).save(any(UserReward.class));
    }

    // ── Speed reward: within 60 mins ──────────────────────────────
    @Test
    void onAllTasksCompleted_within60Mins_grantsSameHourHero() {
        LoginSession session = new LoginSession();
        session.setUserId(1L);
        session.setLoginAt(LocalDateTime.now().minusMinutes(45));
        session.setTasksCompletedAt(null);
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.of(session));
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo, atLeastOnce()).save(any(UserReward.class));
    }

    // ── Streak milestones ─────────────────────────────────────────
    @Test
    void onAllTasksCompleted_streak3_grantsOnARoll() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(3);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "STREAK_3")).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "STREAK_3".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_streak7_grantsWeekWarrior() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(7);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "STREAK_3")).thenReturn(true);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "STREAK_7")).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "STREAK_7".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_streak14_grantsFortnight() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(14);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "STREAK_14".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_streak30_grantsMonthlyMaster() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(30);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "STREAK_30".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_streak60_grantsDiamond() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(60);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "STREAK_60".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_streak100_grantsLegend() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(100);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "STREAK_100".equals(r.getRewardKey())));
    }

    // ── All-time task milestones ───────────────────────────────────
    @Test
    void onAllTasksCompleted_task10_grantsGettingStronger() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(10);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "TASKS_10".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_task25_grantsTaskVeteran() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(25);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "TASKS_25".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_task50_grantsHalfCentury() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(50);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "TASKS_50".equals(r.getRewardKey())));
    }

    @Test
    void onAllTasksCompleted_task100_grantsCenturyClub() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(100);
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "TASKS_100".equals(r.getRewardKey())));
    }

    // ── grantIfNew: skips if already exists ───────────────────────
    @Test
    void onAllTasksCompleted_doesNotGrantDuplicate() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(3);
        when(habitService.allTimeCompleted(1L)).thenReturn(1);
        // All rewards already exist
        when(rewardRepo.existsByUserIdAndRewardKey(eq(1L), anyString())).thenReturn(true);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo, never()).save(any(UserReward.class));
    }

    // ── grantIfNew: title with no space ───────────────────────────
    @Test
    void onAllTasksCompleted_titleWithNoSpace_usesDefaultEmoji() {
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(1);
        when(rewardRepo.existsByUserIdAndRewardKey(1L, "FIRST_TASK")).thenReturn(false);
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        rewardService.onAllTasksCompleted(user);

        verify(rewardRepo).save(argThat(r -> "FIRST_TASK".equals(r.getRewardKey())));
    }

    // ── getRewards ─────────────────────────────────────────────────
    @Test
    void getRewards_returnsListFromRepo() {
        UserReward r1 = new UserReward();
        r1.setRewardKey("STREAK_7");
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(List.of(r1));

        List<UserReward> result = rewardService.getRewards(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRewardKey()).isEqualTo("STREAK_7");
    }

    // ── getRewardCount ─────────────────────────────────────────────
    @Test
    void getRewardCount_returnsCorrectCount() {
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L))
            .thenReturn(List.of(new UserReward(), new UserReward(), new UserReward()));

        assertThat(rewardService.getRewardCount(1L)).isEqualTo(3);
    }

    @Test
    void getRewardCount_returnsZero_whenNoRewards() {
        when(rewardRepo.findByUserIdOrderByEarnedAtDesc(1L)).thenReturn(Collections.emptyList());

        assertThat(rewardService.getRewardCount(1L)).isZero();
    }
}
