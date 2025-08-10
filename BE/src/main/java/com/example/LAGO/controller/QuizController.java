package com.example.LAGO.controller;

import com.example.LAGO.dto.QuizDto;
import com.example.LAGO.exception.ErrorResponse;
import com.example.LAGO.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 퀴즈 컨트롤러
 * /api/study/quiz 경로로 퀴즈 관련 API 제공
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
@Tag(name = "차트학습/퀴즈", description = "투자 학습 및 퀴즈 관련 API")
@Slf4j
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/quiz/random")
    @Operation(
        summary = "랜덤 퀴즈 조회", 
        description = "특정 quiz_id를 제외한 퀴즈 중에서 랜덤하게 하나를 선택하여 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = QuizDto.class))),
        @ApiResponse(responseCode = "404", description = "조건에 맞는 퀴즈가 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "제약조건 위반",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuizDto> getRandomQuiz(
            @Parameter(description = "제외할 퀴즈 ID", example = "1") 
            @RequestParam("quiz_id") Integer quizId) {
        
        log.info("랜덤 퀴즈 조회 요청 - 제외할 quiz_id: {}", quizId);
        
        if (quizId == null) {
            log.warn("quiz_id 파라미터가 누락됨");
            return ResponseEntity.badRequest().build();
        }
        
        Optional<QuizDto> randomQuiz = quizService.getRandomQuizExcluding(quizId);
        
        if (randomQuiz.isPresent()) {
            log.info("랜덤 퀴즈 조회 성공 - 선택된 quiz_id: {}", randomQuiz.get().getQuizId());
            return ResponseEntity.ok(randomQuiz.get());
        } else {
            log.warn("조건에 맞는 퀴즈를 찾을 수 없음 - 제외할 quiz_id: {}", quizId);
            return ResponseEntity.notFound().build();
        }
    }
}