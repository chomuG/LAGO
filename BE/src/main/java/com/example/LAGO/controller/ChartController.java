package com.example.LAGO.controller;

import com.example.LAGO.dto.response.ChartAnalysisResponse;
import com.example.LAGO.service.ChartAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/charts")
@Tag(name = "차트 API", description = "")
@Validated
public class ChartController {

    private final ChartAnalysisService chartAnalysisService;

    @PostMapping("/{stockId}/pattern-analysis")
    @Operation(
            summary = "차트패턴 분석",
            description = "차트에서 감지된 패턴과 근거를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차트패턴 분석 결과 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<ChartAnalysisResponse>> getPatternAnalysis(@PathVariable int stockId) {
        List<ChartAnalysisResponse> detectedPatterns = chartAnalysisService.analyzePatterns(stockId);
        return ResponseEntity.ok(detectedPatterns);
    }
}

