package com.example.LAGO.controller;

import com.example.LAGO.dto.StockMinuteDto;
import com.example.LAGO.service.StockMinuteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/charts/stock-minute")
@Tag(name = "주식 분봉", description = "분봉 주식 정보 API")
public class StockMinuteController {

    private final StockMinuteService stockMinuteService;

    @GetMapping("/{stockId}")
    @Operation(summary = "분봉 주식 정보 조회", description = "지정한 주식의 특정 기간 분봉 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public List<StockMinuteDto> getStockMinutes(
            @Parameter(description = "주식 ID", required = true, example = "1")
            @PathVariable("stockId") Integer stockInfoId,
            @Parameter(description = "시작일시", required = true, example = "2023-01-01T09:00:00")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "종료일시", required = true, example = "2023-12-31T15:30:00")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return stockMinuteService.getMinutes(stockInfoId, start, end);
    }
}
