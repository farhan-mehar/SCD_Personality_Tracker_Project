package com.psyche.service;

import com.psyche.model.User;
import com.psyche.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    @Test
    void emailExists_returnsTrue_whenEmailExists() {
        when(userRepo.existsByEmail("test@example.com")).thenReturn(true);
        assertThat(userService.emailExists("test@example.com")).isTrue();
    }

    @Test
    void emailExists_returnsFalse_whenEmailNotExists() {
        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);
        assertThat(userService.emailExists("new@example.com")).isFalse();
    }

    @Test
    void register_savesUserWithEncodedPassword() {
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        User saved = new User();
        saved.setId(1L);
        saved.setFullName("Jane Doe");
        saved.setEmail("jane@example.com");
        saved.setPassword("encodedPass");
        when(userRepo.save(any(User.class))).thenReturn(saved);

        User result = userService.register("Jane Doe", "jane@example.com", "rawPass");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPass");
        verify(passwordEncoder).encode("rawPass");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void findByEmail_returnsUser_whenFound() {
        User user = new User();
        user.setEmail("found@example.com");
        when(userRepo.findByEmail("found@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("found@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("found@example.com");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void save_delegatesToRepository() {
        User user = new User();
        user.setFullName("Updated Name");
        when(userRepo.save(user)).thenReturn(user);

        User result = userService.save(user);

        assertThat(result.getFullName()).isEqualTo("Updated Name");
        verify(userRepo).save(user);
    }
}
