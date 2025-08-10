package com.example.LAGO.service;

import com.example.LAGO.domain.Quiz;
import com.example.LAGO.dto.QuizDto;
import com.example.LAGO.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 퀴즈 서비스
 * QUIZ 테이블 비즈니스 로직 처리
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final Random random = new Random();

    /**
     * 특정 quiz_id가 아닌 퀴즈 중에서 랜덤하게 하나 선택
     *
     * @param excludeQuizId 제외할 quiz_id
     * @return 랜덤 퀴즈 (Optional)
     */
    public Optional<QuizDto> getRandomQuizExcluding(Integer excludeQuizId) {
        log.debug("랜덤 퀴즈 조회 요청 - 제외할 quiz_id: {}", excludeQuizId);
        
        List<Quiz> availableQuizzes = quizRepository.findAllExcludingQuizId(excludeQuizId);
        
        if (availableQuizzes.isEmpty()) {
            log.warn("제외 조건에 맞는 퀴즈가 없음 - excludeQuizId: {}", excludeQuizId);
            return Optional.empty();
        }
        
        // 랜덤하게 하나 선택
        Quiz randomQuiz = availableQuizzes.get(random.nextInt(availableQuizzes.size()));
        
        log.debug("랜덤 퀴즈 선택됨 - quiz_id: {}, 전체 후보: {}개", randomQuiz.getQuizId(), availableQuizzes.size());
        
        return Optional.of(new QuizDto(randomQuiz));
    }

    /**
     * 모든 퀴즈 조회
     *
     * @return 모든 퀴즈 리스트
     */
    public List<QuizDto> getAllQuizzes() {
        log.debug("모든 퀴즈 조회 요청");
        
        List<Quiz> quizzes = quizRepository.findAllOrderByQuizId();
        
        log.debug("퀴즈 {}개 조회됨", quizzes.size());
        
        return quizzes.stream()
                .map(QuizDto::new)
                .toList();
    }

    /**
     * 일일 퀴즈 조회
     *
     * @param date 날짜
     * @return 일일 퀴즈 (Optional)
     */
    public Optional<QuizDto> getDailyQuiz(LocalDateTime date) {
        log.debug("일일 퀴즈 조회 요청 - date: {}", date);
        
        return quizRepository.findByDailyDate(date)
                .map(QuizDto::new);
    }

    /**
     * 용어 ID로 관련 퀴즈 조회
     *
     * @param termId 투자 용어 ID
     * @return 관련 퀴즈 리스트
     */
    public List<QuizDto> getQuizzesByTermId(Integer termId) {
        log.debug("용어별 퀴즈 조회 요청 - termId: {}", termId);
        
        List<Quiz> quizzes = quizRepository.findByTermId(termId);
        
        log.debug("용어 관련 퀴즈 {}개 조회됨", quizzes.size());
        
        return quizzes.stream()
                .map(QuizDto::new)
                .toList();
    }
}