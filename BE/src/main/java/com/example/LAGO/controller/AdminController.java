package com.example.LAGO.controller;

import com.example.LAGO.domain.DailyQuizSchedule;
import com.example.LAGO.repository.DailyQuizScheduleRepository;
import com.example.LAGO.repository.QuizRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {

    private final DailyQuizScheduleRepository dailyQuizScheduleRepository;
    private final QuizRepository quizRepository;
    private final Random random = new Random();

    @PostMapping("/daily-quiz/schedule-today")
    @Operation(summary = "오늘 데일리 퀴즈 수동 스케줄링", description = "테스트용: 오늘 데일리 퀴즈를 수동으로 스케줄링합니다.")
    public ResponseEntity<String> scheduleToday() {
        LocalDate today = LocalDate.now();
        
        if (dailyQuizScheduleRepository.existsByQuizDate(today)) {
            return ResponseEntity.ok("Today's quiz already scheduled");
        }

        List<com.example.LAGO.domain.Quiz> availableQuizzes = quizRepository.findAll();
        if (availableQuizzes.isEmpty()) {
            return ResponseEntity.badRequest().body("No quizzes available");
        }

        com.example.LAGO.domain.Quiz selectedQuiz = availableQuizzes.get(random.nextInt(availableQuizzes.size()));
        
        DailyQuizSchedule schedule = DailyQuizSchedule.builder()
                .scheduleId((int) System.currentTimeMillis())
                .quizDate(today)
                .quizId(selectedQuiz.getQuizId())
                .startTime(LocalDateTime.now())
                .build();

        dailyQuizScheduleRepository.save(schedule);
        
        return ResponseEntity.ok("Today's quiz scheduled: " + selectedQuiz.getQuestion());
    }
}