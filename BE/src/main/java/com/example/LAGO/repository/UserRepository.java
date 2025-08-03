package com.example.LAGO.repository;

import com.example.LAGO.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPersonalityAndIsDeleted(String personality, boolean isDeleted);
}
