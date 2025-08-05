package com.example.LAGO.repository;

import com.example.LAGO.domain.MockTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MockTradeRepository extends JpaRepository<MockTrade, Long> {
    Long countByAccountId(Long accountId);

    @Query("SELECT AVG(mt.price * mt.quantity) FROM MockTrade mt WHERE mt.account.accountId = :accountId")
    Double findAvgTradeValue(@Param("accountId") Long accountId);
}
