package com.example.LAGO.service;

import com.example.LAGO.constants.Interval;
import com.example.LAGO.domain.*;
import com.example.LAGO.dto.OhlcDataDto;
import com.example.LAGO.dto.response.ChartAnalysisResponse;
import com.example.LAGO.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class ChartAnalysisServiceImpl implements ChartAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ChartAnalysisServiceImpl.class);

    private final WebClient.Builder webClientBuilder;
    private final StockMinuteRepository stockMinuteRepository;
    private final StockDayRepository stockDayRepository;
    private final StockMonthRepository stockMonthRepository;
    private final StockYearRepository stockYearRepository;
    private final StockInfoRepository stockInfoRepository;

    @Value("${services.chart-analysis.url}")
    private String chartAnalysisUrl;

    @Override
    // 임시 테스트 메서드
    public String testPythonConnection() {
        try {
            log.info("Python 서버 헬스 체크를 시도합니다...");
            WebClient webClient = WebClient.create("http://192.168.0.13:5000");
            
            String response = webClient.get()
                    .uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));
            
            log.info("Python 서버 응답 성공: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Python 서버 연결 테스트 실패", e);
            return "Connection failed: " + e.getMessage();
        }
    }

    @Override
    public List<ChartAnalysisResponse> analyzePatterns(Long stockId, Interval interval, LocalDateTime startDate, LocalDateTime endDate) {

        StockInfo stockInfo = stockInfoRepository.findByStockInfoId(stockId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Invalid stock ID: " + stockId));

        List<OhlcDataDto> ohlcData = switch (interval) {
            case MINUTE -> stockMinuteRepository.findByStockInfoAndDateBetweenOrderByDateAsc(stockInfo, startDate, endDate)
                    .stream().map(this::convertMinuteToDto).collect(Collectors.toList());
            case DAY -> stockDayRepository.findByStockInfoStockInfoIdAndNewDateBetweenOrderByNewDateAsc(stockId.intValue(), startDate, endDate)
                    .stream().map(this::convertDayToDto).collect(Collectors.toList());
            case MONTH -> stockMonthRepository.findByStockInfo_StockInfoIdAndNewDateBetweenOrderByNewDateAsc(stockId.intValue(), startDate, endDate)
                    .stream().map(this::convertMonthToDto).collect(Collectors.toList());
            case YEAR -> stockYearRepository.findByStockInfo_StockInfoIdAndNewDateBetweenOrderByNewDateAsc(stockId.intValue(), startDate, endDate)
                    .stream().map(this::convertYearToDto).collect(Collectors.toList());
            default -> Collections.emptyList(); // WEEK 등 미지원 케이스
        };

        log.info("{}개의 {} 데이터를 조회했습니다. (Stock ID: {}, 기간: {} ~ {})",
                ohlcData.size(), interval.getCode(), stockId, startDate, endDate);

        // No Content error - 주가 데이터 없음
        if (ohlcData.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            log.info("Python 분석 서버로 차트 패턴 분석을 요청합니다...");
            WebClient webClient = webClientBuilder.baseUrl(chartAnalysisUrl).build();

            return webClient.post()
                    .bodyValue(ohlcData)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ChartAnalysisResponse>>() {})
                    .block(Duration.ofSeconds(30));
        } catch (WebClientResponseException e) {
            log.error("Python 서버로부터 에러 응답 수신: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Python 서버 분석 요청 실패: " + e.getResponseBodyAsString(), e);
        }
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