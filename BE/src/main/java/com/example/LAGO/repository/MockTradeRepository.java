package com.example.LAGO.repository;

import com.example.LAGO.domain.MockTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MockTradeRepository extends JpaRepository<MockTrade, Long> {
    @Query("SELECT COUNT(mt) FROM MockTrade mt WHERE mt.accountId = :accountId")
    Long countByAccountId(@Param("accountId") Integer accountId);

    @Query("SELECT AVG(mt.price * mt.quantity) FROM MockTrade mt WHERE mt.accountId = :accountId")
    Double findAvgTradeValue(@Param("accountId") Integer accountId);
}
