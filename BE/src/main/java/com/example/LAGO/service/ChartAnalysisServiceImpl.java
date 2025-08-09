package com.example.LAGO.service;

import com.example.LAGO.domain.*;
import com.example.LAGO.dto.OhlcDataDto;
import com.example.LAGO.dto.response.ChartAnalysisResponse;
import com.example.LAGO.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartAnalysisServiceImpl implements ChartAnalysisService {

    private final WebClient.Builder webClientBuilder;
    private final StockMinuteRepository stockMinuteRepository;
    private final StockDayRepository stockDayRepository;
    private final StockMonthRepository stockMonthRepository;
    private final StockYearRepository stockYearRepository;
    private final StockInfoRepository stockInfoRepository; // stockInfo를 조회하기 위해 추가

    @Value("${services.chart-analysis.url}")
    private String chartAnalysisUrl;

    @Override
    public List<ChartAnalysisResponse> analyzePatterns(Long stockId, String interval, LocalDateTime startDate, LocalDateTime endDate) {
        
        StockInfo stockInfo = stockInfoRepository.findByStockInfoId(stockId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Invalid stock ID: " + stockId));

        List<OhlcDataDto> ohlcData = switch (interval.toLowerCase()) {
            case "1m" -> {
                List<StockMinute> minuteData = stockMinuteRepository.findByStockInfoAndDateBetweenOrderByDateAsc(
                        stockInfo, startDate, endDate.plusDays(1));
                yield minuteData.stream().map(this::convertMinuteToDto).collect(Collectors.toList());
            }
            case "1d" -> {
                List<StockDay> dayData = stockDayRepository.findByStockInfoStockInfoIdAndNewDateBetweenOrderByNewDateAsc(
                        stockId.intValue(), startDate, endDate);
                yield dayData.stream().map(this::convertDayToDto).collect(Collectors.toList());
            }
            case "1w" ->
                // 주봉(Week) 관련 로직 추가 필요
                    Collections.emptyList();
            case "1mo" -> {
                List<StockMonth> monthData = stockMonthRepository.findByStockInfo_StockInfoIdAndNewDateBetweenOrderByNewDateAsc(
                        stockId.intValue(), startDate, endDate);
                yield monthData.stream().map(this::convertMonthToDto).collect(Collectors.toList());
            }
            case "1y" -> {
                List<StockYear> yearData = stockYearRepository.findByStockInfo_StockInfoIdAndNewDateBetweenOrderByNewDateAsc(
                        stockId.intValue(), startDate, endDate);
                yield yearData.stream().map(this::convertYearToDto).collect(Collectors.toList());
            }
            default -> throw new IllegalArgumentException("Invalid interval: " + interval);
        };

        if (ohlcData.isEmpty()) {
            return Collections.emptyList();
        }

        WebClient webClient = webClientBuilder.baseUrl(chartAnalysisUrl).build();

        return webClient.post()
                .body(Mono.just(ohlcData), new ParameterizedTypeReference<List<OhlcDataDto>>() {})
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChartAnalysisResponse>>() {})
                .block(Duration.ofSeconds(30));
    }

    // 엔티티 -> DTO 변환 메서드들
    private OhlcDataDto convertMinuteToDto(StockMinute entity) {
        return OhlcDataDto.builder()
                .date(entity.getDate())
                .openPrice(entity.getOpenPrice())
                .highPrice(entity.getHighPrice())
                .lowPrice(entity.getLowPrice())
                .closePrice(entity.getClosePrice())
                .volume(entity.getVolume())
                .build();
    }

    private OhlcDataDto convertDayToDto(StockDay entity) {
        return OhlcDataDto.builder()
                .date(entity.getDate().atStartOfDay())
                .openPrice(entity.getOpenPrice())
                .highPrice(entity.getHighPrice())
                .lowPrice(entity.getLowPrice())
                .closePrice(entity.getClosePrice())
                .volume(entity.getVolume())
                .build();
    }
    
    private OhlcDataDto convertMonthToDto(StockMonth entity) {
        return OhlcDataDto.builder()
                .date(entity.getNewDate())
                .openPrice(entity.getOpenPrice())
                .highPrice(entity.getHighPrice())
                .lowPrice(entity.getLowPrice())
                .closePrice(entity.getClosePrice())
                .volume(entity.getVolume())
                .build();
    }

    private OhlcDataDto convertYearToDto(StockYear entity) {
        return OhlcDataDto.builder()
                .date(entity.getNewDate()) // StockYear는 newDate 필드 사용
                .openPrice(entity.getOpenPrice())
                .highPrice(entity.getHighPrice())
                .lowPrice(entity.getLowPrice())
                .closePrice(entity.getClosePrice())
                .volume(entity.getVolume())
                .build();
    }
}