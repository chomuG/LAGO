package com.example.LAGO.scheduler;

import com.example.LAGO.domain.DailyQuizSchedule;
import com.example.LAGO.repository.DailyQuizScheduleRepository;
import com.example.LAGO.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyQuizScheduler {

    private final DailyQuizScheduleRepository dailyQuizScheduleRepository;
    private final QuizRepository quizRepository;
    private final Random random = new Random();

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void scheduleDailyQuiz() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        if (dailyQuizScheduleRepository.existsByQuizDate(tomorrow)) {
            log.info("Daily quiz already scheduled for {}", tomorrow);
            return;
        }

        List<com.example.LAGO.domain.Quiz> availableQuizzes = quizRepository.findAll();
        if (availableQuizzes.isEmpty()) {
            log.error("No quizzes available for scheduling");
            return;
        }

        com.example.LAGO.domain.Quiz selectedQuiz = availableQuizzes.get(random.nextInt(availableQuizzes.size()));
        
        LocalTime randomTime = generateRandomTime();
        LocalDateTime startTime = tomorrow.atTime(randomTime);

        DailyQuizSchedule schedule = DailyQuizSchedule.builder()
                .scheduleId(generateScheduleId())
                .quizDate(tomorrow)
                .quizId(selectedQuiz.getQuizId())
                .startTime(startTime)
                .build();

        dailyQuizScheduleRepository.save(schedule);
        log.info("Scheduled daily quiz {} for {} at {}", selectedQuiz.getQuizId(), tomorrow, randomTime);
    }

    private LocalTime generateRandomTime() {
        int hour = 16 + random.nextInt(7);
        int minute = random.nextInt(60);
        return LocalTime.of(hour, minute);
    }

    private Integer generateScheduleId() {
        return (int) System.currentTimeMillis();
    }
}