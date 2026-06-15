package com.psyche.service;

import com.psyche.model.DailyTask;
import com.psyche.model.PersonalityTask;
import com.psyche.model.User;
import com.psyche.repository.DailyTaskRepository;
import com.psyche.repository.PersonalityTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock private DailyTaskRepository taskRepo;
    @Mock private PersonalityTaskRepository personalityTaskRepo;
    @InjectMocks private HabitService habitService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setOpenness(80);
        user.setConscientiousness(70);
        user.setExtraversion(60);
        user.setAgreeableness(90);
        user.setNeuroticism(30);
    }

    // ── getTodayTasks: returns existing tasks if already created ───
    @Test
    void getTodayTasks_existingTasksReturned() {
        DailyTask existing = new DailyTask();
        existing.setTrait("Openness");
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(existing));

        List<DailyTask> result = habitService.getTodayTasks(user, "Openness");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrait()).isEqualTo("Openness");
        verify(taskRepo, never()).save(any());
    }

    // ── getTodayTasks: creates new task when none exist ────────────
    @Test
    void getTodayTasks_createsNewTask_whenNoExisting() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        PersonalityTask pt = new PersonalityTask();
        pt.setTrait("Openness");
        pt.setTaskText("Explore something new");
        pt.setTaskNumber(1);
        when(personalityTaskRepo.findByTraitAndScore(eq("Openness"), eq(80)))
            .thenReturn(List.of(pt));

        DailyTask saved = new DailyTask();
        saved.setTrait("Openness");
        saved.setTaskText("Explore something new");
        when(taskRepo.save(any(DailyTask.class))).thenReturn(saved);

        List<DailyTask> result = habitService.getTodayTasks(user, "Openness");

        assertThat(result).hasSize(1);
        verify(taskRepo).save(any(DailyTask.class));
    }

    // ── getTodayTasks: returns empty list if no personality tasks ──
    @Test
    void getTodayTasks_returnsEmpty_whenNoPersonalityTasks() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(personalityTaskRepo.findByTraitAndScore(anyString(), anyInt()))
            .thenReturn(Collections.emptyList());

        List<DailyTask> result = habitService.getTodayTasks(user, "Openness");

        assertThat(result).isEmpty();
    }

    // ── getTodayTasks: each trait mapped correctly ─────────────────
    @Test
    void getTodayTasks_conscientiousnessTraitScore() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(personalityTaskRepo.findByTraitAndScore(eq("Conscientiousness"), eq(70)))
            .thenReturn(Collections.emptyList());

        habitService.getTodayTasks(user, "Conscientiousness");
        verify(personalityTaskRepo).findByTraitAndScore("Conscientiousness", 70);
    }

    @Test
    void getTodayTasks_extraversionTraitScore() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(personalityTaskRepo.findByTraitAndScore(eq("Extraversion"), eq(60)))
            .thenReturn(Collections.emptyList());

        habitService.getTodayTasks(user, "Extraversion");
        verify(personalityTaskRepo).findByTraitAndScore("Extraversion", 60);
    }

    @Test
    void getTodayTasks_agreeablenessTraitScore() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(personalityTaskRepo.findByTraitAndScore(eq("Agreeableness"), eq(90)))
            .thenReturn(Collections.emptyList());

        habitService.getTodayTasks(user, "Agreeableness");
        verify(personalityTaskRepo).findByTraitAndScore("Agreeableness", 90);
    }

    @Test
    void getTodayTasks_neuroticismTraitScore() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(personalityTaskRepo.findByTraitAndScore(eq("Neuroticism"), eq(30)))
            .thenReturn(Collections.emptyList());

        habitService.getTodayTasks(user, "Neuroticism");
        verify(personalityTaskRepo).findByTraitAndScore("Neuroticism", 30);
    }

    @Test
    void getTodayTasks_unknownTrait_defaultScore50() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(personalityTaskRepo.findByTraitAndScore(eq("Unknown"), eq(50)))
            .thenReturn(Collections.emptyList());

        habitService.getTodayTasks(user, "Unknown");
        verify(personalityTaskRepo).findByTraitAndScore("Unknown", 50);
    }

    // ── toggleTask ─────────────────────────────────────────────────
    @Test
    void toggleTask_togglesCompletionState() {
        DailyTask task = new DailyTask();
        task.setId(5L);
        task.setCompleted(false);
        when(taskRepo.findById(5L)).thenReturn(Optional.of(task));

        habitService.toggleTask(5L);

        assertThat(task.isCompleted()).isTrue();
        verify(taskRepo).save(task);
    }

    @Test
    void toggleTask_togglesTrueToFalse() {
        DailyTask task = new DailyTask();
        task.setId(5L);
        task.setCompleted(true);
        when(taskRepo.findById(5L)).thenReturn(Optional.of(task));

        habitService.toggleTask(5L);

        assertThat(task.isCompleted()).isFalse();
    }

    @Test
    void toggleTask_doesNothing_whenNotFound() {
        when(taskRepo.findById(99L)).thenReturn(Optional.empty());

        habitService.toggleTask(99L);

        verify(taskRepo, never()).save(any());
    }

    // ── calculateGlobalStreak ──────────────────────────────────────
    @Test
    void calculateGlobalStreak_returnsZero_whenNoTasks() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        int streak = habitService.calculateGlobalStreak(1L);
        assertThat(streak).isZero();
    }

    @Test
    void calculateGlobalStreak_returnOne_whenTodayDone() {
        DailyTask done = new DailyTask();
        done.setCompleted(true);

        // Today has done tasks, yesterday does not
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now())))
            .thenReturn(List.of(done));
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now().minusDays(1))))
            .thenReturn(Collections.emptyList());

        int streak = habitService.calculateGlobalStreak(1L);
        assertThat(streak).isEqualTo(1);
    }

    @Test
    void calculateGlobalStreak_skipsEmptyToday_checksYesterday() {
        DailyTask notDone = new DailyTask();
        notDone.setCompleted(false);

        // Today: empty (skip) → yesterday: done
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now())))
            .thenReturn(Collections.emptyList());
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now().minusDays(1))))
            .thenReturn(List.of(notDone));

        // Yesterday has task but not done, so streak=0
        int streak = habitService.calculateGlobalStreak(1L);
        assertThat(streak).isZero();
    }

    @Test
    void calculateGlobalStreak_multiDay() {
        DailyTask done = new DailyTask();
        done.setCompleted(true);

        // Today + yesterday + day before: done; 3 days before: empty
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now())))
            .thenReturn(List.of(done));
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now().minusDays(1))))
            .thenReturn(List.of(done));
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now().minusDays(2))))
            .thenReturn(List.of(done));
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), eq(LocalDate.now().minusDays(3))))
            .thenReturn(Collections.emptyList());

        int streak = habitService.calculateGlobalStreak(1L);
        assertThat(streak).isEqualTo(3);
    }

    // ── Today stats ────────────────────────────────────────────────
    @Test
    void totalCompletedToday_countsOnlyCompleted() {
        DailyTask done = makeTask(true);
        DailyTask notDone = makeTask(false);
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(done, done, notDone));

        assertThat(habitService.totalCompletedToday(1L)).isEqualTo(2);
    }

    @Test
    void totalTasksToday_returnsAllTasks() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(makeTask(true), makeTask(false), makeTask(false)));

        assertThat(habitService.totalTasksToday(1L)).isEqualTo(3);
    }

    @Test
    void allTimeCompleted_returnsCount() {
        when(taskRepo.findByUserIdAndCompletedTrue(1L))
            .thenReturn(List.of(makeTask(true), makeTask(true), makeTask(true)));

        assertThat(habitService.allTimeCompleted(1L)).isEqualTo(3);
    }

    @Test
    void todayPercent_returnsCorrectPercentage() {
        DailyTask done = makeTask("Openness", true);
        DailyTask notDone = makeTask("Openness", false);
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(done, notDone));

        assertThat(habitService.todayPercent(1L, "Openness")).isEqualTo(50);
    }

    @Test
    void todayPercent_returnsZero_whenNoTraitTasks() {
        DailyTask otherTrait = makeTask("Extraversion", true);
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(otherTrait));

        assertThat(habitService.todayPercent(1L, "Openness")).isZero();
    }

    @Test
    void allTodayDone_returnsTrue_whenAllComplete() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(makeTask(true), makeTask(true)));

        assertThat(habitService.allTodayDone(1L)).isTrue();
    }

    @Test
    void allTodayDone_returnsFalse_whenSomeIncomplete() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(List.of(makeTask(true), makeTask(false)));

        assertThat(habitService.allTodayDone(1L)).isFalse();
    }

    @Test
    void allTodayDone_returnsFalse_whenEmpty() {
        when(taskRepo.findByUserIdAndTaskDate(eq(1L), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        assertThat(habitService.allTodayDone(1L)).isFalse();
    }

    // ── streakEmoji ────────────────────────────────────────────────
    @Test
    void streakEmoji_allBranches() {
        assertThat(habitService.streakEmoji(0)).isEqualTo("❄️");
        assertThat(habitService.streakEmoji(1)).isEqualTo("🌱");
        assertThat(habitService.streakEmoji(2)).isEqualTo("🌱");
        assertThat(habitService.streakEmoji(3)).isEqualTo("🔥");
        assertThat(habitService.streakEmoji(6)).isEqualTo("🔥");
        assertThat(habitService.streakEmoji(7)).isEqualTo("⚡");
        assertThat(habitService.streakEmoji(13)).isEqualTo("⚡");
        assertThat(habitService.streakEmoji(14)).isEqualTo("🌟");
        assertThat(habitService.streakEmoji(29)).isEqualTo("🌟");
        assertThat(habitService.streakEmoji(30)).isEqualTo("🏆");
        assertThat(habitService.streakEmoji(100)).isEqualTo("🏆");
    }

    // ── streakMsg ──────────────────────────────────────────────────
    @Test
    void streakMsg_allBranches() {
        assertThat(habitService.streakMsg(0)).contains("Start");
        assertThat(habitService.streakMsg(1)).contains("Great");
        assertThat(habitService.streakMsg(2)).contains("Building");
        assertThat(habitService.streakMsg(3)).contains("fire");
        assertThat(habitService.streakMsg(6)).contains("fire");
        assertThat(habitService.streakMsg(7)).contains("ONE WEEK");
        assertThat(habitService.streakMsg(10)).contains("Unstoppable");
        assertThat(habitService.streakMsg(14)).contains("TWO WEEKS");
        assertThat(habitService.streakMsg(20)).contains("Elite");
        assertThat(habitService.streakMsg(30)).contains("LEGENDARY");
    }

    // ── traitColor ─────────────────────────────────────────────────
    @Test
    void traitColor_allTraits() {
        assertThat(habitService.traitColor("Openness")).isEqualTo("#f97316");
        assertThat(habitService.traitColor("Conscientiousness")).isEqualTo("#3b82f6");
        assertThat(habitService.traitColor("Extraversion")).isEqualTo("#eab308");
        assertThat(habitService.traitColor("Agreeableness")).isEqualTo("#22c55e");
        assertThat(habitService.traitColor("Neuroticism")).isEqualTo("#a855f7");
        assertThat(habitService.traitColor("Unknown")).isEqualTo("#6366f1");
    }

    // ── Helpers ────────────────────────────────────────────────────
    private DailyTask makeTask(boolean completed) {
        DailyTask t = new DailyTask();
        t.setCompleted(completed);
        return t;
    }

    private DailyTask makeTask(String trait, boolean completed) {
        DailyTask t = new DailyTask();
        t.setTrait(trait);
        t.setCompleted(completed);
        return t;
    }
}
