package com.example.LAGO.repository;

import com.example.LAGO.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserIdAndType(Long userId, String type);
}
