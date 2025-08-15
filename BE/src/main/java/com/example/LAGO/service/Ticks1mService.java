package com.example.LAGO.service;

import com.example.LAGO.domain.Ticks1m;
import com.example.LAGO.dto.Ticks1mDto;
import com.example.LAGO.repository.Ticks1mRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class Ticks1mService {
    
    private final Ticks1mRepository ticks1mRepository;
    
    /**
     * 특정 종목의 기간별 1분봉 데이터 조회 (KST 입력 -> UTC 조회 -> KST 응답)
     * 
     * @param code 종목 코드 (예: "005930")
     * @param startDateKst 시작 시간 (KST 기준)
     * @param endDateKst 종료 시간 (KST 기준)
     * @return 1분봉 데이터 리스트 (KST 시간으로 변환)
     */
    public List<Ticks1mDto> get1mCandlesByCodeAndDateRange(String code, LocalDateTime startDateKst, LocalDateTime endDateKst) {
        try {
            log.info("1분봉 데이터 조회 시작: code={}, start={}, end={}", code, startDateKst, endDateKst);
            
            // KST -> UTC 변환
            ZoneId kstZone = ZoneId.of("Asia/Seoul");
            ZoneId utcZone = ZoneId.of("UTC");
            
            OffsetDateTime startTimeUtc = startDateKst.atZone(kstZone)
                    .withZoneSameInstant(utcZone)
                    .toOffsetDateTime();
            OffsetDateTime endTimeUtc = endDateKst.atZone(kstZone)
                    .withZoneSameInstant(utcZone)
                    .toOffsetDateTime();
            
            log.debug("시간 변환 완료: startUtc={}, endUtc={}", startTimeUtc, endTimeUtc);
            
            // UTC 기준으로 DB 조회
            List<Ticks1m> ticks1mList = ticks1mRepository.findByCodeAndBucketRange(code, startTimeUtc, endTimeUtc);
            
            log.info("1분봉 데이터 조회 완료: code={}, 조회된 건수={}", code, ticks1mList.size());
            
            // Entity -> DTO 변환 (내부에서 UTC -> KST 변환)
            return ticks1mList.stream()
                    .map(Ticks1mDto::fromEntity)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("1분봉 데이터 조회 실패: code={}, start={}, end={}", code, startDateKst, endDateKst, e);
            return List.of();
        }
    }
    
    /**
     * 특정 종목의 최신 1분봉 데이터 조회 (최대 100개)
     * 
     * @param code 종목 코드
     * @return 최신 1분봉 데이터 (KST 시간으로 변환)
     */
    public List<Ticks1mDto> getLatest1mCandlesByCode(String code) {
        try {
            log.info("최신 1분봉 데이터 조회: code={}", code);
            
            List<Ticks1m> ticks1mList = ticks1mRepository.findLatestByCode(code, Pageable.ofSize(100));
            
            log.info("최신 1분봉 데이터 조회 완료: code={}, 조회된 건수={}", code, ticks1mList.size());
            
            return ticks1mList.stream()
                    .map(Ticks1mDto::fromEntity)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("최신 1분봉 데이터 조회 실패: code={}", code, e);
            return List.of();
        }
    }
}