package com.example.LAGO.controller;

import com.example.LAGO.dto.StockInfoDto;
import com.example.LAGO.service.StockInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockInfoController {

    private final StockInfoService stockInfoService;

    @GetMapping
    @Operation(summary = "주식 종목 전체 조회", description = "전체 주식 종목을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "주식 종목을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<List<StockInfoDto>> getAllStockInfo() {
        List<StockInfoDto> stockInfos = stockInfoService.getAllStockInfo();
        return ResponseEntity.ok(stockInfos);
    }
}
