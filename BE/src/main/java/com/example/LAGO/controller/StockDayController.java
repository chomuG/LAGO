package com.example.LAGO.controller;

import com.example.LAGO.dto.StockDayDto;
import com.example.LAGO.service.StockDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/charts/stock-day")
@Tag(name = "주식 일별", description = "일별 주식 정보 API")
public class StockDayController {

    private final StockDayService stockDayService;

    @GetMapping("/{stockId}")
    @Operation(summary = "일별 주식 정보 조회", description = "지정한 주식의 특정 기간 일별 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public List<StockDayDto> getStockDays(
            @Parameter(description = "주식 ID", required = true, example = "1")
            @PathVariable("stockId") Integer stockInfoId,
            @Parameter(description = "시작일", required = true, example = "2023-01-01")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "종료일", required = true, example = "2023-12-31")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return stockDayService.getStockDays(stockInfoId, start, end);
    }
}
