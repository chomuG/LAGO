package com.example.LAGO.controller;

import com.example.LAGO.service.AutoTradingBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 자동매매봇 테스트 컨트롤러
 * 개발/테스트 환경에서 자동매매봇 동작을 수동으로 테스트하기 위한 API
 */
@RestController
@RequestMapping("/api/test/auto-trading")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "자동매매 테스트", description = "자동매매봇 테스트 API")
public class AutoTradingTestController {

    private final AutoTradingBotService autoTradingBotService;

    /**
     * 자동매매 즉시 실행 (스케줄러 대기 없이)
     */
    @PostMapping("/execute")
    @Operation(summary = "자동매매 즉시 실행", description = "스케줄러를 기다리지 않고 즉시 자동매매를 실행합니다.")
    public ResponseEntity<Map<String, String>> executeAutoTradingNow() {
        try {
            log.info("수동 자동매매 실행 요청");
            autoTradingBotService.executeAutoTrading();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "자동매매가 실행되었습니다. 로그를 확인하세요."
            ));
        } catch (Exception e) {
            log.error("수동 자동매매 실행 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error", 
                "message", "자동매매 실행 중 오류: " + e.getMessage()
            ));
        }
    }

    /**
     * 자동매매 설정 정보 조회
     */
    @GetMapping("/info")
    @Operation(summary = "자동매매 설정 정보", description = "현재 자동매매 설정 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getAutoTradingInfo() {
        return ResponseEntity.ok(Map.of(
            "interval_ms", 60000L,
            "initial_delay_ms", 30000L,
            "description", "1분마다 실행되는 자동매매봇",
            "test_url", "/api/test/auto-trading/execute"
        ));
    }
}