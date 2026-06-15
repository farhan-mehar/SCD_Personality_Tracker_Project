package com.psyche.controller;

import com.psyche.model.DailyTask;
import com.psyche.model.LoginSession;
import com.psyche.model.User;
import com.psyche.model.UserReward;
import com.psyche.repository.LoginSessionRepository;
import com.psyche.repository.QuizHistoryRepository;
import com.psyche.service.HabitService;
import com.psyche.service.RewardService;
import com.psyche.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.psyche.config.LoginSuccessHandler;
import com.psyche.service.UserDetailsServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// NO wildcard hamcrest import — use only Mockito's any() to avoid ambiguity
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private HabitService habitService;
    @MockBean private RewardService rewardService;
    @MockBean private QuizHistoryRepository quizHistoryRepo;
    @MockBean private LoginSessionRepository sessionRepo;

    // ── Required so SecurityConfig's @Autowired fields resolve in this slice ──
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private LoginSuccessHandler loginSuccessHandler;
    @MockBean private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setOpenness(50);
        user.setConscientiousness(50);
        user.setExtraversion(50);
        user.setAgreeableness(50);
        user.setNeuroticism(70); // high = weak
        user.setQuizCompleted(true);

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    }

    // ── GET /dashboard ─────────────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void dashboard_returns200_withAllAttributes() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(3);
        when(habitService.streakEmoji(3)).thenReturn("🔥");
        when(habitService.streakMsg(3)).thenReturn("On fire!");
        when(habitService.getTodayTasks(eq(user), anyString())).thenReturn(Collections.emptyList());
        when(habitService.todayPercent(eq(1L), anyString())).thenReturn(0);
        when(habitService.totalCompletedToday(1L)).thenReturn(0);
        when(habitService.totalTasksToday(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(5);
        when(rewardService.getRewardCount(1L)).thenReturn(2);
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attributeExists("globalStreak"))
            .andExpect(model().attributeExists("traitCards"))
            .andExpect(model().attribute("globalStreak", 3));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void dashboard_withLoginSession_calculatesTimerRemaining() throws Exception {
        LoginSession session = new LoginSession();
        session.setUserId(1L);
        session.setLoginAt(LocalDateTime.now().minusMinutes(3));

        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.streakEmoji(0)).thenReturn("❄️");
        when(habitService.streakMsg(0)).thenReturn("Start your streak!");
        when(habitService.getTodayTasks(eq(user), anyString())).thenReturn(Collections.emptyList());
        when(habitService.todayPercent(eq(1L), anyString())).thenReturn(0);
        when(habitService.totalCompletedToday(1L)).thenReturn(0);
        when(habitService.totalTasksToday(1L)).thenReturn(0);
        when(habitService.allTimeCompleted(1L)).thenReturn(0);
        when(rewardService.getRewardCount(1L)).thenReturn(0);
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.of(session));

        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("timerSecsRemaining"))
            .andExpect(model().attributeExists("showSpeedTimer"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void dashboard_allDone_setsAllDoneFlag() throws Exception {
        DailyTask done = new DailyTask();
        done.setTrait("Neuroticism");
        done.setCompleted(true);

        when(habitService.calculateGlobalStreak(1L)).thenReturn(1);
        when(habitService.streakEmoji(1)).thenReturn("🌱");
        when(habitService.streakMsg(1)).thenReturn("Great start!");
        when(habitService.getTodayTasks(eq(user), anyString())).thenReturn(List.of(done));
        when(habitService.todayPercent(eq(1L), anyString())).thenReturn(100);
        when(habitService.totalCompletedToday(1L)).thenReturn(1);
        when(habitService.totalTasksToday(1L)).thenReturn(1);
        when(habitService.allTimeCompleted(1L)).thenReturn(1);
        when(rewardService.getRewardCount(1L)).thenReturn(0);
        when(sessionRepo.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("allDoneToday", true));
    }

    // ── POST /tasks/{id}/toggle ────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void toggleTask_returnsJsonResponse() throws Exception {
        when(habitService.totalCompletedToday(1L)).thenReturn(1);
        when(habitService.totalTasksToday(1L)).thenReturn(2);
        when(habitService.allTodayDone(1L)).thenReturn(false);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(2);
        when(habitService.streakEmoji(2)).thenReturn("🌱");
        when(rewardService.getRewardCount(1L)).thenReturn(0);

        mockMvc.perform(post("/tasks/5/toggle").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.totalDoneToday").value(1))
            .andExpect(jsonPath("$.totalTasksToday").value(2))
            .andExpect(jsonPath("$.allDoneToday").value(false))
            .andExpect(jsonPath("$.globalStreak").value(2));

        verify(habitService).toggleTask(5L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void toggleTask_allDone_grantsRewards() throws Exception {
        UserReward reward = new UserReward();
        reward.setEmoji("🔥");
        reward.setTitle("On a Roll");
        reward.setDescription("3-day streak!");
        reward.setBadgeColor("#f97316");

        when(habitService.totalCompletedToday(1L)).thenReturn(5);
        when(habitService.totalTasksToday(1L)).thenReturn(5);
        when(habitService.allTodayDone(1L)).thenReturn(true);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(3);
        when(habitService.streakEmoji(3)).thenReturn("🔥");
        when(rewardService.onAllTasksCompleted(user)).thenReturn(List.of(reward));
        when(rewardService.getRewardCount(1L)).thenReturn(1);

        mockMvc.perform(post("/tasks/5/toggle").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allDoneToday").value(true))
            .andExpect(jsonPath("$.newRewards").isArray())
            .andExpect(jsonPath("$.newRewards[0].emoji").value("🔥"));
    }

    // ── GET /learn ─────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void learn_returnsAboutView() throws Exception {
        mockMvc.perform(get("/learn"))
            .andExpect(status().isOk())
            .andExpect(view().name("about"));
    }

    // ── GET /profile ───────────────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void profile_returns200_withTraitStats() throws Exception {
        when(habitService.allTimeCompleted(1L)).thenReturn(10);
        when(habitService.totalCompletedToday(1L)).thenReturn(1);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(5);
        when(habitService.streakEmoji(5)).thenReturn("🔥");
        when(habitService.traitColor(anyString())).thenReturn("#aabbcc");
        when(quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(1L)).thenReturn(Collections.emptyList());
        when(rewardService.getRewards(1L)).thenReturn(Collections.emptyList());
        when(rewardService.getRewardCount(1L)).thenReturn(0);

        mockMvc.perform(get("/profile"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile"))
            .andExpect(model().attributeExists("traitStats"))
            .andExpect(model().attributeExists("quizHistory"))
            .andExpect(model().attributeExists("rewards"));
    }

    // ── POST /profile/update ───────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_updatesName_redirectsToProfile() throws Exception {
        // Use explicit Mockito any(User.class) — no Hamcrest wildcard to cause ambiguity
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/profile/update").with(csrf())
                .param("fullName", "Updated Name"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile"));

        verify(userService).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_blankName_doesNotSave() throws Exception {
        mockMvc.perform(post("/profile/update").with(csrf())
                .param("fullName", "  "))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile"));

        verify(userService, never()).save(any(User.class));
    }

    // ── getWeakTraits static helper ────────────────────────────────
    @Test
    void getWeakTraits_correctlyIdentifiesWeakTraits() {
        User u = new User();
        u.setOpenness(50);           // < 60 → weak
        u.setConscientiousness(65);  // >= 60 → not weak
        u.setExtraversion(40);       // < 60 → weak
        u.setAgreeableness(55);      // < 60 → weak
        u.setNeuroticism(70);        // > 60 → weak

        List<String> weak = MainController.getWeakTraits(u);

        assertThat(weak)
            .containsExactlyInAnyOrder("Openness", "Extraversion", "Agreeableness", "Neuroticism")
            .doesNotContain("Conscientiousness");
    }

    @Test
    void getWeakTraits_highScores_returnsEmpty() {
        User u = new User();
        u.setOpenness(70);
        u.setConscientiousness(80);
        u.setExtraversion(90);
        u.setAgreeableness(75);
        u.setNeuroticism(30);

        List<String> weak = MainController.getWeakTraits(u);

        assertThat(weak).isEmpty();
    }
}
