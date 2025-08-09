package com.example.LAGO.service;

import com.example.LAGO.dto.request.ChartPatternAnalysisRequest;
import com.example.LAGO.dto.response.ChartAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChartAnalysisServiceImpl implements ChartAnalysisService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.chart-analysis.url}")
    private String chartAnalysisUrl;

    @Override
    public List<ChartAnalysisResponse> analyzePatterns(int stockId) {
        WebClient webClient = webClientBuilder.baseUrl(chartAnalysisUrl).build();

        ChartPatternAnalysisRequest requestDto = new ChartPatternAnalysisRequest(stockId);

        return webClient.post()
                .body(Mono.just(requestDto), ChartPatternAnalysisRequest.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChartAnalysisResponse>>() {})
                .block(Duration.ofSeconds(30)); // 타임아웃 15초 설정
    }
}
