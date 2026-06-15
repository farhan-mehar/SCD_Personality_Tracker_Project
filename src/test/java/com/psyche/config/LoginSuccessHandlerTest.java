package com.psyche.config;

import com.psyche.model.LoginSession;
import com.psyche.model.User;
import com.psyche.repository.LoginSessionRepository;
import com.psyche.repository.UserRepository;
import com.psyche.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginSuccessHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private LoginSessionRepository sessionRepo;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;
    @Mock private RedirectStrategy redirectStrategy;

    @InjectMocks private LoginSuccessHandler loginSuccessHandler;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setQuizCompleted(true);

        when(authentication.getName()).thenReturn("test@example.com");
        loginSuccessHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    void onAuthenticationSuccess_quizCompleted_savesSessionAndSendsEmail() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(sessionRepo.save(any(LoginSession.class))).thenAnswer(i -> i.getArgument(0));

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // Verify session saved
        ArgumentCaptor<LoginSession> sessionCaptor = ArgumentCaptor.forClass(LoginSession.class);
        verify(sessionRepo).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getUserId()).isEqualTo(1L);

        // Verify email triggered
        verify(emailService).sendLoginEmailAfterDelay(1L);

        // Verify redirect
        verify(redirectStrategy).sendRedirect(request, response, "/dashboard");
    }

    @Test
    void onAuthenticationSuccess_quizNotCompleted_skipsSessionAndEmail() throws Exception {
        user.setQuizCompleted(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(sessionRepo, never()).save(any());
        verify(emailService, never()).sendLoginEmailAfterDelay(anyLong());
        verify(redirectStrategy).sendRedirect(request, response, "/dashboard");
    }

    @Test
    void onAuthenticationSuccess_userNotFound_redirects() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(sessionRepo, never()).save(any());
        verify(emailService, never()).sendLoginEmailAfterDelay(anyLong());
        verify(redirectStrategy).sendRedirect(request, response, "/dashboard");
    }
}
