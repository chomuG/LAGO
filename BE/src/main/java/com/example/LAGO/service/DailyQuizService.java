package com.example.LAGO.service;

import com.example.LAGO.domain.*;
import com.example.LAGO.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyQuizService {

    private final DailyQuizScheduleRepository dailyQuizScheduleRepository;
    private final QuizRepository quizRepository;
    private final DailySolvedRepository dailySolvedRepository;
    private final KnowTermRepository knowTermRepository;

    public Quiz getTodayQuiz() {
        LocalDate today = LocalDate.now();
        
        DailyQuizSchedule schedule = dailyQuizScheduleRepository.findByQuizDate(today)
                .orElseThrow(() -> new RuntimeException("No daily quiz scheduled for today"));

        return quizRepository.findById(schedule.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public DailyQuizResult getTodayQuizForUser(Integer userId) {
        LocalDate today = LocalDate.now();
        
        DailyQuizSchedule schedule = dailyQuizScheduleRepository.findByQuizDate(today)
                .orElseThrow(() -> new RuntimeException("No daily quiz scheduled for today"));
        
        Quiz quiz = quizRepository.findById(schedule.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Optional<DailySolved> solved = dailySolvedRepository.findByUserIdAndQuizIdAndSolvedAt(
                userId, quiz.getQuizId(), today);

        if (solved.isPresent()) {
            DailySolved dailySolved = solved.get();
            return DailyQuizResult.builder()
                    .alreadySolved(true)
                    .quiz(null)
                    .solvedAt(dailySolved.getSolvedAt().toString())
                    .score(dailySolved.getScore())
                    .ranking(dailySolved.getRanking())
                    .build();
        } else {
            return DailyQuizResult.builder()
                    .alreadySolved(false)
                    .quiz(quiz)
                    .solvedAt(null)
                    .score(null)
                    .ranking(null)
                    .build();
        }
    }

    @Transactional
    public SolveResult solveDailyQuiz(Integer userId, Integer quizId, Boolean userAnswer, Integer solvedTimeSeconds) {
        LocalDate today = LocalDate.now();
        
        if (dailySolvedRepository.existsByUserIdAndQuizIdAndSolvedAt(userId, quizId, today)) {
            throw new RuntimeException("Already solved today's quiz");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        boolean isCorrect = quiz.getAnswer().equals(userAnswer);
        int score = isCorrect ? 100 : 0;

        int ranking = calculateRanking(quizId, today, solvedTimeSeconds);
        int bonusAmount = calculateBonusAmount(ranking);

        DailySolved dailySolved = DailySolved.builder()
                .solvedId(generateSolvedId())
                .userId(userId)
                .quizId(quizId)
                .score(score)
                .solvedAt(today)
                .solvedTimeSeconds(solvedTimeSeconds)
                .ranking(ranking)
                .bonusAmount(bonusAmount)
                .build();

        dailySolvedRepository.save(dailySolved);

        updateKnowTerm(userId, quiz.getTermId(), isCorrect);

        return SolveResult.builder()
                .correct(isCorrect)
                .score(score)
                .ranking(ranking)
                .bonusAmount(bonusAmount)
                .explanation(quiz.getExplanation())
                .build();
    }

    private int calculateRanking(Integer quizId, LocalDate today, Integer solvedTimeSeconds) {
        Long fasterSolvers = dailySolvedRepository.countFasterSolvers(quizId, today, solvedTimeSeconds);
        return fasterSolvers.intValue() + 1;
    }

    private int calculateBonusAmount(int ranking) {
        return switch (ranking) {
            case 1 -> 100000;
            case 2 -> 50000;
            case 3 -> 30000;
            default -> {
                if (ranking <= 10) yield 10000;
                else yield 2000;
            }
        };
    }

    private void updateKnowTerm(Integer userId, Integer termId, boolean correct) {
        Optional<KnowTerm> existingKnowTerm = knowTermRepository.findByUserIdAndTermId(userId, termId);
        
        if (existingKnowTerm.isPresent()) {
            KnowTerm knowTerm = existingKnowTerm.get();
            knowTerm.setCorrect(correct);
            knowTermRepository.save(knowTerm);
            log.info("Updated know_term for user {} term {} correct {}", userId, termId, correct);
        } else {
            KnowTerm newKnowTerm = KnowTerm.builder()
                    .knowId(generateKnowId())
                    .userId(userId)
                    .termId(termId)
                    .correct(correct)
                    .build();
            knowTermRepository.save(newKnowTerm);
            log.info("Created new know_term for user {} term {} correct {}", userId, termId, correct);
        }
    }

    private Integer generateKnowId() {
        return (int) System.currentTimeMillis();
    }

    private Integer generateSolvedId() {
        return (int) System.currentTimeMillis();
    }

    public static class DailyQuizResult {
        public boolean alreadySolved;
        public Quiz quiz;
        public String solvedAt;
        public Integer score;
        public Integer ranking;

        public static DailyQuizResultBuilder builder() {
            return new DailyQuizResultBuilder();
        }

        public static class DailyQuizResultBuilder {
            private boolean alreadySolved;
            private Quiz quiz;
            private String solvedAt;
            private Integer score;
            private Integer ranking;

            public DailyQuizResultBuilder alreadySolved(boolean alreadySolved) {
                this.alreadySolved = alreadySolved;
                return this;
            }

            public DailyQuizResultBuilder quiz(Quiz quiz) {
                this.quiz = quiz;
                return this;
            }

            public DailyQuizResultBuilder solvedAt(String solvedAt) {
                this.solvedAt = solvedAt;
                return this;
            }

            public DailyQuizResultBuilder score(Integer score) {
                this.score = score;
                return this;
            }

            public DailyQuizResultBuilder ranking(Integer ranking) {
                this.ranking = ranking;
                return this;
            }

            public DailyQuizResult build() {
                DailyQuizResult result = new DailyQuizResult();
                result.alreadySolved = this.alreadySolved;
                result.quiz = this.quiz;
                result.solvedAt = this.solvedAt;
                result.score = this.score;
                result.ranking = this.ranking;
                return result;
            }
        }
    }

    public static class SolveResult {
        public boolean correct;
        public int score;
        public int ranking;
        public int bonusAmount;
        public String explanation;

        public static SolveResultBuilder builder() {
            return new SolveResultBuilder();
        }

        public static class SolveResultBuilder {
            private boolean correct;
            private int score;
            private int ranking;
            private int bonusAmount;
            private String explanation;

            public SolveResultBuilder correct(boolean correct) {
                this.correct = correct;
                return this;
            }

            public SolveResultBuilder score(int score) {
                this.score = score;
                return this;
            }

            public SolveResultBuilder ranking(int ranking) {
                this.ranking = ranking;
                return this;
            }

            public SolveResultBuilder bonusAmount(int bonusAmount) {
                this.bonusAmount = bonusAmount;
                return this;
            }

            public SolveResultBuilder explanation(String explanation) {
                this.explanation = explanation;
                return this;
            }

            public SolveResult build() {
                SolveResult result = new SolveResult();
                result.correct = this.correct;
                result.score = this.score;
                result.ranking = this.ranking;
                result.bonusAmount = this.bonusAmount;
                result.explanation = this.explanation;
                return result;
            }
        }
    }
}