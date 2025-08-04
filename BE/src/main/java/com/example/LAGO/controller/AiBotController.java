package com.example.LAGO.controller;

import com.example.LAGO.dto.AiBotAccountResponse;
import com.example.LAGO.service.AiBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI 매매봇 관련 API 컨트롤러
 * 명세서 기준: GET /api/ai-bots/{strategyId}/{stockId}, POST /api/ai-bots/{strategyId}/customize 등
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-bots")
@Tag(name = "AI 매매봇", description = "AI 매매봇 전략, 거래, 계좌 관리 API")
@Validated
public class AiBotController {

    private final AiBotService aiBotService;

    /**
     * AI 매매봇 계좌 조회
     * 지침서 명세: AI 봇은 user 테이블의 is_ai 컬럼으로 구분
     * 
     * @param aiId AI 봇 식별자 (ai_id 컬럼값)
     * @return AI 봇 계좌 정보
     */
    @GetMapping("/{aiId}/account")
    @Operation(
        summary = "AI 매매봇 계좌 조회",
        description = "특정 AI 매매봇의 계좌 정보를 조회합니다. 지침서 명세서 기준으로 is_ai=true인 사용자의 계좌를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "AI 봇을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<AiBotAccountResponse> getAiBotAccount(
            @Parameter(description = "AI 봇 식별자 (1-10)", required = true, example = "1")
            @PathVariable 
            @NotNull(message = "AI ID는 필수입니다") 
            @Positive(message = "AI ID는 양수여야 합니다") 
            Integer aiId
    ) {
        AiBotAccountResponse response = aiBotService.getAiBotAccount(aiId);
        return ResponseEntity.ok(response);
    }

    /**
     * AI 매매봇 상태 조회 (기존 메서드 - 호환성 유지)
     * 
     * @param aiId AI 봇 식별자
     * @return AI 봇 상태 정보
     * @deprecated /api/ai-bots/{aiId}/account 사용 권장
     */
    @GetMapping("/status")
    @Operation(
        summary = "AI 매매봇 상태 조회 (Deprecated)",
        description = "AI 매매봇 상태를 조회합니다. /api/ai-bots/{aiId}/account 사용을 권장합니다.",
        deprecated = true
    )
    @Deprecated
    public ResponseEntity<AiBotAccountResponse> getAiBotStatus(
            @Parameter(description = "AI 봇 식별자", required = true, example = "1")
            @RequestParam 
            @NotNull(message = "AI ID는 필수입니다") 
            @Positive(message = "AI ID는 양수여야 합니다") 
            Integer aiId
    ) {
        AiBotAccountResponse response = aiBotService.getAiBotAccount(aiId);
        return ResponseEntity.ok(response);
    }
}
