package com.psyche.repository;

import com.psyche.model.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {
    Optional<LoginSession> findTopByUserIdOrderByLoginAtDesc(Long userId);
}
