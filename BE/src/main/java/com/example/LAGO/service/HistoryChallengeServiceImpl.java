package com.example.LAGO.service;

import com.example.LAGO.constants.Interval;
import com.example.LAGO.domain.HistoryChallenge;
import com.example.LAGO.domain.HistoryChallengeData;
import com.example.LAGO.dto.response.HistoryChallengeDataResponse;
import com.example.LAGO.dto.response.HistoryChallengeResponse;
import com.example.LAGO.exception.NoContentException;
import com.example.LAGO.repository.HistoryChallengeDataRepository;
import com.example.LAGO.repository.HistoryChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryChallengeServiceImpl implements HistoryChallengeService {

    private static final Logger log = LoggerFactory.getLogger(HistoryChallengeServiceImpl.class);

    private final HistoryChallengeRepository historyChallengeRepository;
    private final HistoryChallengeDataRepository historyChallengeDataRepository;

    @Override
    public HistoryChallengeDataResponse getLatestData() {
        // TODO: '상태 관리'

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        HistoryChallengeData latest = historyChallengeDataRepository.findLatestChallengeData(now);

        return new HistoryChallengeDataResponse(latest);
    }

    @Override
    public HistoryChallengeResponse getHistoryChallenge() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 1. 현재 날짜로 진행 중인 챌린지 조회
        HistoryChallenge challenge = historyChallengeRepository.findByDate(now);
        if (challenge == null) {
            throw new NoContentException("현재 진행 중인 역사 챌린지가 없습니다.");
        }

        // 2. 해당 챌린지의 가장 최신 주가 데이터 조회
        HistoryChallengeData currentData = historyChallengeDataRepository.findLatestChallengeData(now);

        // 3. 두 엔티티를 사용하여 응답 DTO 생성 및 반환
        return new HistoryChallengeResponse(challenge, currentData);
    }

    @Override
    public List<HistoryChallengeDataResponse> getHistoryChallengeData(Integer challengeId, Interval interval) {

        // 0. 챌린지 정보 조회
        historyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge ID: " + challengeId));

        // 1. 현재 일시
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 2. interval 문자열 매핑
        String intervalString = Interval.intervalToString(interval);

        // 3. DB 조회 (현재 시간까지)
        List<Object[]> aggregatedData = historyChallengeDataRepository.findAggregatedByChallengeIdAndDate(
                challengeId,
                now,
                intervalString
        );

        if (aggregatedData == null || aggregatedData.isEmpty()) {
            throw new NoContentException("해당 챌린지의 차트 데이터가 없습니다.");
        }

        return aggregatedData.stream()
                .map(HistoryChallengeDataResponse::new)
                .collect(Collectors.toList());
    }

//    public List<News> getChallengeNews() {
//        HistoryChallenge challenge = historyChallengeRepository.findByDate(LocalDate.now());
//        if (challenge == null) {
//            throw new NoContentException("현재 진행 중인 역사 챌린지가 없습니다.");
//        }
//
//        // 1. 현재 이벤트 진행 시간 계산
//        LocalDateTime now = LocalDateTime.now();
//        long minutesSinceStart = Duration.between(event.getStartTime(), now).toMinutes();
//
//        // 2. 분 단위 → 과거 시간으로 변환
//        LocalDateTime pastTime = event.getPastStartTime().plusMinutes(minutesSinceStart);
//
//        // 3. 뉴스 조회 (과거 날짜로)
//        return newsService.getNewsByDate(pastTime.toLocalDate());
//    }
}