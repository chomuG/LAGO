package com.example.LAGO.service;

import com.example.LAGO.domain.HistoryChallenge;
import com.example.LAGO.domain.HistoryChallengeData;
import com.example.LAGO.dto.response.HistoryChallengeDataResponse;
import com.example.LAGO.dto.response.HistoryChallengeResponse;
import com.example.LAGO.repository.HistoryChallengeDataRepository;
import com.example.LAGO.repository.HistoryChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryChallengeServiceImpl implements HistoryChallengeService {

    private final HistoryChallengeRepository historyChallengeRepository;
    private final HistoryChallengeDataRepository historyChallengeDataRepository;

    @Override
    public HistoryChallengeResponse getHistoryChallenge() {
        HistoryChallenge challenge = historyChallengeRepository.findByDate(LocalDate.now());
        if (challenge == null) {
            throw new IllegalStateException("현재 진행 중인 역사 챌린지가 없습니다.");
        }
        return new HistoryChallengeResponse(challenge);
    }

    @Override
    public List<HistoryChallengeDataResponse> getHistoryChallengeData(Integer challengeId, String interval) {
        HistoryChallenge challenge = historyChallengeRepository.findByDate(LocalDate.now());
        if (challenge == null) {
            throw new IllegalStateException("현재 진행 중인 역사 챌린지가 없습니다.");
        }

        switch (interval.toUpperCase()) {
            case "1D":
                List<HistoryChallengeData> dayData = historyChallengeDataRepository.findByIntervalTypeAndDate(challengeId, interval, LocalDateTime.now());
                return dayData.stream()
                        .map(HistoryChallengeDataResponse::new)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("지원하지 않는 간격입니다: " + interval);
        }
    }
}