package com.example.LAGO.controller;

import com.example.LAGO.dto.Ticks1mDto;
import com.example.LAGO.service.Ticks1mService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
@Tag(name = "1분봉 데이터", description = "1분봉 집계 데이터 조회 API")
public class Ticks1mController {
    
    private final Ticks1mService ticks1mService;
    
    @GetMapping("/{code}/1m")
    @Operation(
            summary = "종목별 기간 1분봉 데이터 조회", 
            description = "특정 종목의 지정 기간 1분봉 집계 데이터를 조회합니다 (KST 기준)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<List<Ticks1mDto>> get1mCandles(
            @Parameter(description = "종목 코드", required = true, example = "005930")
            @PathVariable("code") String code,
            @Parameter(description = "시작 시간 (KST)", required = true, example = "2024-08-15T09:00:00")
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 시간 (KST)", required = true, example = "2024-08-15T15:30:00")
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        // 디버깅 로그
        System.out.println("🔍 1분봉 데이터 요청:");
        System.out.println("code: " + code);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        
        List<Ticks1mDto> result = ticks1mService.get1mCandlesByCodeAndDateRange(code, startDate, endDate);
        
        System.out.println("📊 1분봉 조회 결과: " + result.size() + "건");
        if (!result.isEmpty()) {
            System.out.println("첫 번째 데이터: " + result.get(0).getBucket());
            System.out.println("마지막 데이터: " + result.get(result.size() - 1).getBucket());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{code}/latest")
    @Operation(
            summary = "종목별 최신 1분봉 데이터 조회", 
            description = "특정 종목의 최신 1분봉 데이터(100개)를 조회합니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<List<Ticks1mDto>> getLatest1mCandles(
            @Parameter(description = "종목 코드", required = true, example = "005930")
            @PathVariable("code") String code
    ) {
        List<Ticks1mDto> result = ticks1mService.getLatest1mCandlesByCode(code);
        
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }
}