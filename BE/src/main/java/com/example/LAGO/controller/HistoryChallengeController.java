package com.example.LAGO.controller;

import com.example.LAGO.constants.ChallengeInterval;
import com.example.LAGO.dto.response.HistoryChallengeDataResponse;
import com.example.LAGO.dto.response.HistoryChallengeResponse;
import com.example.LAGO.service.HistoryChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history-challenge")
@Tag(name = "역사챌린지 API", description = "역사챌린지 정보 및 차트를 조회합니다.")
@Validated
public class HistoryChallengeController {

    private final HistoryChallengeService historyChallengeService;

    @GetMapping()
    @Operation(
            summary = "역사챌린지 조회",
            description = "현재 진행 중인 역사챌린지 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역사챌린지 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<HistoryChallengeResponse> getHistoryChallenge() {
        HistoryChallengeResponse response = historyChallengeService.getHistoryChallenge();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{challengeId}")
    @Operation(
            summary = "역사챌린지 차트 조회",
            description = "현재 진행 중인 역사챌린지 차트를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역사챌린지 차트 조회 성공"),
            @ApiResponse(responseCode = "204", description = "진행 중인 역사챌린지 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<HistoryChallengeDataResponse>> getHistoryChallengeChart(
            @Parameter(description = "챌린지 ID", required = true, example = "1")
            @Validated @PathVariable Integer challengeId,
            @Parameter(description = "간격", required = true, example = "1D")
            @Validated @RequestParam ChallengeInterval interval
    ) {
        List<HistoryChallengeDataResponse> response = historyChallengeService.getHistoryChallengeData(challengeId, interval);
        return ResponseEntity.ok(response);
    }
}
