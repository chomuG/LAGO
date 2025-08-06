package com.example.LAGO.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.LAGO.domain.StockInfo;
public interface StockInfoRepository extends JpaRepository<StockInfo, Integer> {

}
