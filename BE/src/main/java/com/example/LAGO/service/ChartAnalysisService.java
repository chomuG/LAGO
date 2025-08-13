package com.example.LAGO.service;

import com.example.LAGO.dto.response.ChartAnalysisResponse;
import com.example.LAGO.constants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ChartAnalysisService {
    // 임시 테스트 메서드
    String testPythonConnection();

    /**
     * 주어진 주식 ID에 대한 차트 패턴 분석을 요청합니다.
     *
     * @param stockId 분석할 주식의 ID
     * @return 감지된 차트 패턴 목록
     */
    List<ChartAnalysisResponse> analyzePatterns(String stockCode, ChartMode chartMode, Interval interval, LocalDateTime fromTimeDate, LocalDateTime toDateTime);
}