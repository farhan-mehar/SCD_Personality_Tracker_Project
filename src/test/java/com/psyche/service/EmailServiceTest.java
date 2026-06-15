package com.psyche.service;

import com.psyche.model.DailyTask;
import com.psyche.model.User;
import com.psyche.repository.DailyTaskRepository;
import com.psyche.repository.UserRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private UserRepository userRepo;
    @Mock private HabitService habitService;
    @Mock private DailyTaskRepository taskRepo;
    @InjectMocks private EmailService emailService;

    private User user;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Fatima");
        user.setEmail("fatimazahra15029@gmail.com");
        user.setQuizCompleted(true);

        mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // ── sendDailyReminders ─────────────────────────────────────────
    @Test
    void sendDailyReminders_sendsEmailToQuizCompletedUsers() throws Exception {
        when(userRepo.findAll()).thenReturn(List.of(user));
        when(habitService.calculateGlobalStreak(1L)).thenReturn(5);
        when(habitService.streakEmoji(5)).thenReturn("🔥");
        when(habitService.streakMsg(5)).thenReturn("On fire!");

        emailService.sendDailyReminders();

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendDailyReminders_skipsUsersWithoutQuizCompleted() {
        User noQuiz = new User();
        noQuiz.setId(2L);
        noQuiz.setEmail("fatimazahra59524@gmail.com");
        noQuiz.setQuizCompleted(false);
        when(userRepo.findAll()).thenReturn(List.of(noQuiz));

        emailService.sendDailyReminders();

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendDailyReminders_handlesMailException_gracefully() {
        when(userRepo.findAll()).thenReturn(List.of(user));
        when(habitService.calculateGlobalStreak(anyLong())).thenReturn(0);
        when(habitService.streakEmoji(0)).thenReturn("❄️");
        when(habitService.streakMsg(0)).thenReturn("Start your streak!");
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server down"));

        // Should not throw
        emailService.sendDailyReminders();
    }

    // ── sendDailyEmail ─────────────────────────────────────────────
    @Test
    void sendDailyEmail_sendsEmail() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(3);
        when(habitService.streakEmoji(3)).thenReturn("🔥");
        when(habitService.streakMsg(3)).thenReturn("On fire!");

        emailService.sendDailyEmail(user);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // ── sendCongratulationsEmail ───────────────────────────────────
    @Test
    void sendCongratulationsEmail_sendsEmail() throws Exception {
        DailyTask done = new DailyTask();
        done.setTrait("Openness");
        done.setCompleted(true);
        when(habitService.calculateGlobalStreak(1L)).thenReturn(7);
        when(habitService.streakEmoji(7)).thenReturn("⚡");
        when(habitService.streakMsg(7)).thenReturn("ONE WEEK!");

        emailService.sendCongratulationsEmail(user, List.of(done));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // ── sendEscalatingReminderEmail: all 4 waves ───────────────────
    @Test
    void sendEscalatingReminderEmail_wave1() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(5);
        when(habitService.streakEmoji(5)).thenReturn("🔥");

        DailyTask pending = new DailyTask();
        pending.setTrait("Openness");
        pending.setCompleted(false);
        pending.setTaskText("Try something new");

        DailyTask done = new DailyTask();
        done.setTrait("Conscientiousness");
        done.setCompleted(true);

        emailService.sendEscalatingReminderEmail(user, List.of(pending, done), 1, 5);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEscalatingReminderEmail_wave2() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.streakEmoji(0)).thenReturn("❄️");

        DailyTask pending = new DailyTask();
        pending.setTrait("Openness");
        pending.setCompleted(false);
        pending.setTaskText("Read something new");

        emailService.sendEscalatingReminderEmail(user, List.of(pending), 2, 30);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEscalatingReminderEmail_wave3() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.streakEmoji(0)).thenReturn("❄️");

        DailyTask pending = new DailyTask();
        pending.setTrait("Neuroticism");
        pending.setCompleted(false);
        pending.setTaskText("Meditate for 5 minutes");

        emailService.sendEscalatingReminderEmail(user, List.of(pending), 3, 120);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEscalatingReminderEmail_wave4_default() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.streakEmoji(0)).thenReturn("❄️");

        DailyTask pending = new DailyTask();
        pending.setTrait("Agreeableness");
        pending.setCompleted(false);
        pending.setTaskText("Help someone today");

        emailService.sendEscalatingReminderEmail(user, List.of(pending), 4, 360);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEscalatingReminderEmail_emptyDoneList() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(2);
        when(habitService.streakEmoji(2)).thenReturn("🌱");

        DailyTask pending = new DailyTask();
        pending.setTrait("Extraversion");
        pending.setCompleted(false);
        pending.setTaskText("Talk to a stranger");

        // no done tasks → doneHtml should be empty
        emailService.sendEscalatingReminderEmail(user, List.of(pending), 1, 5);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEscalatingReminderEmail_noStreak_noStreakRiskHtml() throws Exception {
        when(habitService.calculateGlobalStreak(1L)).thenReturn(0);
        when(habitService.streakEmoji(0)).thenReturn("❄️");

        DailyTask pending = new DailyTask();
        pending.setTrait("Openness");
        pending.setCompleted(false);
        pending.setTaskText("Explore");

        // streak = 0 → streakRiskHtml is empty string
        emailService.sendEscalatingReminderEmail(user, List.of(pending), 2, 30);

        verify(mailSender).send(any(MimeMessage.class));
    }

    // ── sendLoginEmailAfterDelay_legacy ────────────────────────────
    @Test
    void sendLoginEmailAfterDelay_legacy_callsMainMethod() {
        // Just call it — it delegates to sendLoginEmailAfterDelay async method
        // We verify no exception is thrown (actual async not invoked in unit test context)
        User noQuiz = new User();
        noQuiz.setId(99L);
        noQuiz.setQuizCompleted(false);
        when(userRepo.findById(99L)).thenReturn(Optional.of(noQuiz));

        // Run synchronously enough to verify it doesn't crash
        emailService.sendLoginEmailAfterDelay_legacy(99L);
    }
}
