package com.example.LAGO.service;

import com.example.LAGO.domain.HistoryChallenge;
import com.example.LAGO.domain.HistoryChallengeData;
import com.example.LAGO.constants.Interval;
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
    public List<HistoryChallengeDataResponse> getHistoryChallengeData(Integer challengeId, Interval interval) {
        HistoryChallenge challenge = historyChallengeRepository.findById(challengeId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge ID: " + challengeId));

        // enum의 code 값을 사용하여 Repository 호출
        List<HistoryChallengeData> data = historyChallengeDataRepository.findByIntervalTypeAndDate(
                challengeId, 
                interval.getCode(), 
                LocalDateTime.now()
        );
        
        return data.stream()
                .map(HistoryChallengeDataResponse::new)
                .collect(Collectors.toList());
    }
}
