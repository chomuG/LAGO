package com.example.LAGO.ai.sentiment;

import com.example.LAGO.ai.sentiment.dto.SentimentRequestDto;
import com.example.LAGO.ai.sentiment.dto.SentimentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * FinBERT 감정 분석 서비스
 * 
 * 지침서 명세:
 * - FinBERT Flask 서버를 활용한 뉴스 감정 분석 서비스
 * - 감정 점수(-1.0 ~ 1.0)를 캐릭터별 AI 매매 전략에 활용
 * - Java 21 Virtual Thread를 활용한 고성능 비동기 처리
 * - Redis 캐싱을 통한 중복 분석 방지 및 성능 최적화
 * 
 * 캐릭터별 활용 방식:
 * - 화끈이: 긍정 신호에 강하게 반응하는 공격적 매매
 * - 적극이: 성장주 관점에서 감정 점수 활용
 * - 균형이: 분산투자 관점에서 신중한 감정 점수 반영
 * - 조심이: 부정 신호에 민감하게 반응하는 보수적 매매
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FinBertSentimentService {

    // ======================== 의존성 주입 ========================

    /**
     * FinBERT HTTP 클라이언트
     */
    private final SentimentAnalysisClient sentimentAnalysisClient;

    // ======================== Virtual Thread Executor ========================
    // Virtual Thread 처리는 SentimentAnalysisClient에서 담당

    // ======================== 공개 메서드 ========================

    /**
     * 단일 뉴스 URL에 대한 감정 분석 (비동기)
     * 
     * @param request 감정 분석 요청
     * @return 비동기 감정 분석 결과
     */
    public CompletableFuture<SentimentResponseDto> analyzeSingleNewsAsync(SentimentRequestDto request) {
        log.info("단일 뉴스 감정 분석 시작: url={}, userId={}", request.getUrl(), request.getUserId());
        
        return sentimentAnalysisClient.analyzeSentimentAsync(request)
            .thenApply(response -> {
                if (response.getSuccess()) {
                    log.info("단일 뉴스 감정 분석 완료: url={}, score={}, label={}", 
                            request.getUrl(), response.getSentimentScore(), response.getSentimentLabel());
                } else {
                    log.error("단일 뉴스 감정 분석 실패: url={}, error={}", 
                            request.getUrl(), response.getErrorMessage());
                }
                return response;
            });
    }

    /**
     * 단일 뉴스 URL에 대한 감정 분석 (동기)
     * 
     * @param request 감정 분석 요청
     * @return 감정 분석 결과
     */
    @Cacheable(value = "sentiment_analysis", key = "#request.url", unless = "#result.success == false")
    public SentimentResponseDto analyzeSingleNews(SentimentRequestDto request) {
        log.info("동기 뉴스 감정 분석 시작: url={}", request.getUrl());
        return sentimentAnalysisClient.analyzeSentiment(request);
    }

    /**
     * 다중 뉴스 URL에 대한 배치 감정 분석 (비동기)
     * 
     * Virtual Thread를 활용하여 여러 뉴스를 동시에 분석
     * 
     * @param requests 감정 분석 요청 리스트
     * @return 비동기 감정 분석 결과 리스트
     */
    public CompletableFuture<List<SentimentResponseDto>> analyzeBatchNewsAsync(List<SentimentRequestDto> requests) {
        log.info("배치 뉴스 감정 분석 시작: count={}", requests.size());

        // Virtual Thread로 동시 처리
        List<CompletableFuture<SentimentResponseDto>> futures = requests.stream()
            .map(this::analyzeSingleNewsAsync)
            .toList();

        // 모든 분석 완료 대기
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<SentimentResponseDto> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
                
                long successCount = results.stream()
                    .mapToLong(r -> r.getSuccess() ? 1 : 0)
                    .sum();
                
                log.info("배치 뉴스 감정 분석 완료: total={}, success={}, failed={}", 
                        requests.size(), successCount, requests.size() - successCount);
                
                return results;
            });
    }

    /**
     * 특정 주식에 관련된 뉴스들의 종합 감정 점수 계산
     * 
     * 여러 뉴스의 감정 점수를 가중평균하여 종합 점수 산출
     * 
     * @param stockCode 주식 코드
     * @param newsUrls 관련 뉴스 URL 리스트
     * @param userId 요청 사용자 ID
     * @return 종합 감정 분석 결과
     */
    public CompletableFuture<SentimentResponseDto> analyzeStockRelatedNewsAsync(
            String stockCode, List<String> newsUrls, Integer userId) {
        
        log.info("주식 관련 뉴스 종합 감정 분석 시작: stockCode={}, newsCount={}, userId={}", 
                stockCode, newsUrls.size(), userId);

        // 요청 리스트 생성
        List<SentimentRequestDto> requests = newsUrls.stream()
            .map(url -> SentimentRequestDto.builder()
                .url(url)
                .stockCode(stockCode)
                .userId(userId)
                .build())
            .toList();

        // 배치 분석 수행
        return analyzeBatchNewsAsync(requests)
            .thenApply(results -> calculateAggregatedSentiment(stockCode, results));
    }

    /**
     * 캐릭터별 맞춤 감정 분석
     * 
     * 각 캐릭터의 투자 성향에 맞춰 감정 점수를 조정
     * 
     * @param request 감정 분석 요청
     * @param characterName 캐릭터명 (화끈이, 적극이, 균형이, 조심이)
     * @return 캐릭터 맞춤 감정 분석 결과
     */
    public CompletableFuture<SentimentResponseDto> analyzeForCharacterAsync(
            SentimentRequestDto request, String characterName) {
        
        log.info("캐릭터별 맞춤 감정 분석 시작: character={}, url={}", characterName, request.getUrl());

        return analyzeSingleNewsAsync(request)
            .thenApply(response -> adjustSentimentForCharacter(response, characterName));
    }

    // ======================== 내부 메서드 ========================

    /**
     * 여러 뉴스의 감정 점수를 종합하여 최종 점수 계산
     * 
     * @param stockCode 주식 코드
     * @param results 개별 뉴스 감정 분석 결과
     * @return 종합 감정 분석 결과
     */
    private SentimentResponseDto calculateAggregatedSentiment(String stockCode, List<SentimentResponseDto> results) {
        // 성공한 분석 결과만 필터링
        List<SentimentResponseDto> successResults = results.stream()
            .filter(SentimentResponseDto::getSuccess)
            .toList();

        if (successResults.isEmpty()) {
            log.warn("주식 관련 뉴스 분석 결과 없음: stockCode={}", stockCode);
            return SentimentResponseDto.builder()
                .success(false)
                .errorMessage("분석 가능한 뉴스가 없습니다")
                .build();
        }

        // 가중평균 계산 (신뢰도를 가중치로 사용)
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (SentimentResponseDto result : successResults) {
            double weight = result.getConfidence() != null ? result.getConfidence() : 0.5;
            totalWeightedScore += result.getSentimentScore() * weight;
            totalWeight += weight;
        }

        double aggregatedScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
        double avgConfidence = successResults.stream()
            .mapToDouble(r -> r.getConfidence() != null ? r.getConfidence() : 0.5)
            .average()
            .orElse(0.5);

        // 종합 감정 라벨 결정
        String aggregatedLabel = determineAggregatedLabel(aggregatedScore);

        log.info("주식 관련 뉴스 종합 감정 분석 완료: stockCode={}, aggregatedScore={}, confidence={}, newsCount={}", 
                stockCode, aggregatedScore, avgConfidence, successResults.size());

        return SentimentResponseDto.builder()
            .success(true)
            .sentimentScore(aggregatedScore)
            .sentimentLabel(aggregatedLabel)
            .confidence(avgConfidence)
            .newsTitle(String.format("%s 관련 뉴스 %d건 종합 분석", stockCode, successResults.size()))
            .newsSummary(String.format("총 %d건의 뉴스를 분석하여 종합 감정 점수를 산출했습니다.", successResults.size()))
            .build();
    }

    /**
     * 종합 감정 점수를 기반으로 라벨 결정
     * 
     * @param score 종합 감정 점수
     * @return 감정 라벨
     */
    private String determineAggregatedLabel(double score) {
        if (score >= 0.1) return "POSITIVE";
        if (score <= -0.1) return "NEGATIVE";
        return "NEUTRAL";
    }

    /**
     * 캐릭터별 특성에 맞춰 감정 점수 조정
     * 
     * @param originalResponse 원본 감정 분석 결과
     * @param characterName 캐릭터명
     * @return 조정된 감정 분석 결과
     */
    private SentimentResponseDto adjustSentimentForCharacter(SentimentResponseDto originalResponse, String characterName) {
        if (!originalResponse.getSuccess()) {
            return originalResponse;
        }

        double originalScore = originalResponse.getSentimentScore();
        double adjustedScore = originalScore;

        // 캐릭터별 감정 점수 조정
        switch (characterName) {
            case "화끈이" -> {
                // 긍정 신호를 더 강하게, 부정 신호도 더 강하게 반응
                adjustedScore = originalScore * 1.3;
            }
            case "적극이" -> {
                // 긍정 신호에 더 민감하게 반응
                adjustedScore = originalScore > 0 ? originalScore * 1.2 : originalScore * 0.9;
            }
            case "균형이" -> {
                // 극단적인 신호를 완화
                adjustedScore = originalScore * 0.8;
            }
            case "조심이" -> {
                // 부정 신호에 더 민감하게, 긍정 신호는 보수적으로
                adjustedScore = originalScore < 0 ? originalScore * 1.2 : originalScore * 0.7;
            }
        }

        // 점수 범위 제한 (-1.0 ~ 1.0)
        adjustedScore = Math.max(-1.0, Math.min(1.0, adjustedScore));

        log.info("캐릭터별 감정 점수 조정: character={}, original={}, adjusted={}", 
                characterName, originalScore, adjustedScore);

        // 조정된 결과 반환 (Builder 패턴으로 새 객체 생성)
        return SentimentResponseDto.builder()
            .success(originalResponse.getSuccess())
            .sentimentScore(adjustedScore)
            .sentimentLabel(determineAggregatedLabel(adjustedScore))
            .confidence(originalResponse.getConfidence())
            .newsTitle(originalResponse.getNewsTitle())
            .newsSummary(originalResponse.getNewsSummary())
            .hwakkeunSignal(originalResponse.getHwakkeunSignal())
            .jeokgeukSignal(originalResponse.getJeokgeukSignal())
            .gyunhyungSignal(originalResponse.getGyunhyungSignal())
            .josimSignal(originalResponse.getJosimSignal())
            .analyzedAt(originalResponse.getAnalyzedAt())
            .processingTimeMs(originalResponse.getProcessingTimeMs())
            .build();
    }
}
