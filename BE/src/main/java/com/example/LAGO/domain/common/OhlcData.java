package com.example.LAGO.domain.common;

import java.time.LocalDateTime;

/**
 * 모든 OHLCV 데이터 엔티티가 구현할 공통 인터페이스
 */
public interface OhlcData {
    LocalDateTime getDateTime();
    int getOpenPrice();
    int getHighPrice();
    int getLowPrice();
    int getClosePrice();
    long getVolume();
}
