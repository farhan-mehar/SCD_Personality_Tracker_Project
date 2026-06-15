package com.psyche.config;

import com.psyche.model.LoginSession;
import com.psyche.model.User;
import com.psyche.repository.LoginSessionRepository;
import com.psyche.repository.UserRepository;
import com.psyche.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private LoginSessionRepository sessionRepo;

    public LoginSuccessHandler() { super("/dashboard"); }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = authentication.getName();

        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isQuizCompleted()) {
                // Record login session for timer tracking
                LoginSession session = new LoginSession();
                session.setUserId(user.getId());
                sessionRepo.save(session);

                // Fire escalating email reminders
                emailService.sendLoginEmailAfterDelay(user.getId());
            }
        });

        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
}
