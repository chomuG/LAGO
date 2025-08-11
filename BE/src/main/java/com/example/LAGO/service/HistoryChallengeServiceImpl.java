package com.example.LAGO.service;

import com.example.LAGO.constants.ChallengeInterval;
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

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryChallengeServiceImpl implements HistoryChallengeService {

    private static final Logger log = LoggerFactory.getLogger(HistoryChallengeServiceImpl.class);
    private static final int PLAYBACK_SPEED_FACTOR = 5; // 5배

    private final HistoryChallengeRepository historyChallengeRepository;
    private final HistoryChallengeDataRepository historyChallengeDataRepository;

    @Override
    public HistoryChallengeResponse getHistoryChallenge() {

        LocalDateTime now = LocalDateTime.now();

        // 1. 현재 날짜로 진행 중인 챌린지 조회
        HistoryChallenge challenge = historyChallengeRepository.findByDate(now);
        if (challenge == null) {
            throw new NoContentException("현재 진행 중인 역사 챌린지가 없습니다.");
        }

        // 2. 해당 챌린지의 가장 최신 주가 데이터 조회
        HistoryChallengeData currentData = historyChallengeDataRepository.findTopByChallengeIdOrderByEventDateDesc(challenge.getChallengeId(), now);

        // 3. 두 엔티티를 사용하여 응답 DTO 생성 및 반환
        return new HistoryChallengeResponse(challenge, currentData);
    }

    @Override
    public List<HistoryChallengeDataResponse> getHistoryChallengeData(Integer challengeId, ChallengeInterval interval) {

        // 0. 챌린지 정보 조회
        HistoryChallenge challenge = historyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge ID: " + challengeId));

        // 0. 챌린지 정보 세팅
        LocalDateTime eventStartDate = challenge.getStartDate(); // 게임 시작(실제) 시각
        LocalDateTime originStartDate = challenge.getOriginDate(); // 과거 데이터 시작 시각
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        LocalTime eventStartTime = LocalTime.of(15, 0);   // 하루 이벤트 시작시간
        LocalTime eventEndTime = LocalTime.of(22, 0);     // 하루 이벤트 종료시간

        // 1. 이벤트가 시작된 날짜부터 오늘까지 경과한 전체 ‘이벤트일 수’ 계산 (하루 단위)
        long eventDayCount = ChronoUnit.DAYS.between(eventStartDate.toLocalDate(), now.toLocalDate());
        if (eventDayCount < 0) eventDayCount = 0;

        log.info("eventDayCount: {}", eventDayCount);

        // 2. 오늘 하루 이벤트 내 진행된 분 계산 (3시~10시 기준)
        // 현재 시간이 3시 이전이면 0분, 10시 이후면 7*60분 (하루 최대 진행시간)
        LocalTime nowTime = now.toLocalTime();
        int todayMinutesPassed;
        if (nowTime.isBefore(eventStartTime)) { // 3시 이전
            log.info("nowTime: {}", nowTime);
            todayMinutesPassed = 0;
        } else if (nowTime.isAfter(eventEndTime)) { // 10시 이후
            todayMinutesPassed = 7 * 60;
        } else {
            todayMinutesPassed = (int) ChronoUnit.MINUTES.between(eventStartTime, nowTime);
        }

        log.info("todayMinutesPassed: {}", todayMinutesPassed);

        LocalDate pastDate = originStartDate.toLocalDate().plusDays(eventDayCount * 7 + (int)(todayMinutesPassed / 60));
        LocalTime pastTime = LocalTime.of(9, 0).plusMinutes((todayMinutesPassed % 60) * 24);
        LocalDateTime pastDateTime = LocalDateTime.of(pastDate, pastTime);

        log.info("pastDate: {}", pastDate);
        log.info("pastTime: {}", pastTime);
        log.info("pastDateTime: {}", pastDateTime);

//        // 3. 총 진행 슬롯 수 = (지난 이벤트일 * slotsPerDay) + 오늘 진행된 슬롯 수
//        int totalSlotsPassed = (int) (eventDayCount * slotsPerDay + (todayMinutesPassed / minutesPerSlot));
//
//        // 4. 하루 7슬롯이 5거래일에 매핑 → 총 진행 거래일 수 계산
//        int tradingDaysPassed = (totalSlotsPassed * 7) / slotsPerDay;
//
//        // 5. 과거 시작일 기준으로 거래일 수 더하기 (주말 건너뛰기 필요 없다면 plusDays 사용)
//        // LocalDate virtualDate = addTradingDays(originDate.toLocalDate(), tradingDaysPassed);
//        LocalDate virtualDate = originStartDate.toLocalDate().plusDays(tradingDaysPassed);
//
//        // 6. 하루 중 진행 중인 슬롯의 시간 계산
//        int slotInDay = totalSlotsPassed % slotsPerDay;
//        LocalTime virtualTime = eventStartTime.plusMinutes(slotInDay * minutesPerSlot);
//
//        // 7. 최종 가상 현재 시각
//        LocalDateTime virtualCurrentTime = LocalDateTime.of(virtualDate, virtualTime);
//
//        log.info("가상 현재 시간: {}, 실제 경과일: {}, 오늘 진행 분: {}, 총 슬롯: {}, 총 거래일 진행: {}",
//                virtualCurrentTime, eventDayCount, todayMinutesPassed, totalSlotsPassed, tradingDaysPassed);

        // 8. interval 문자열 매핑
        String intervalString = switch (interval) {
            case MINUTE -> "24 minute";
            case MINUTE5 -> "2 hour";
            case MINUTE10 -> "4 hour";
            case MINUTE30 -> "12 hour";
            case HOUR -> "1 day";
            case DAY -> "1 week";
            case WEEK -> "7 week";
            case MONTH -> "1 month";
            case YEAR -> "1 year";
        };

        // 9. DB 조회 (가상 현재 시간까지)
        List<Object[]> aggregatedData = historyChallengeDataRepository.findAggregatedByChallengeIdAndDate(
                challengeId,
                pastDateTime,
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