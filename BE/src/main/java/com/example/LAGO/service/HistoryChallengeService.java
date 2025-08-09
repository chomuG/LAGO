package com.example.LAGO.service;

import com.example.LAGO.dto.response.ChartAnalysisResponse;
import com.example.LAGO.dto.response.HistoryChallengeDataResponse;
import com.example.LAGO.dto.response.HistoryChallengeResponse;

import java.util.List;

public interface HistoryChallengeService {
    /**
     * 현재 진행 중인 역사챌린지 정보를 반환합니다.
     *
     * @return 역사챌린지 정보
     */
    HistoryChallengeResponse getHistoryChallenge();

    /**
     * 현재 시점 까지의 역사챌린지 주가 정보 목록을 반환합니다.
     *
     * @param interval 간격
     * @return 역사챌린지 주가 정보 목록
     */
    List<HistoryChallengeDataResponse> getHistoryChallengeData(Integer challengeId, String interval);
}
