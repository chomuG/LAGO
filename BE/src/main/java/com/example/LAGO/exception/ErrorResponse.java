package com.example.LAGO.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 에러 응답 DTO
 * 지침서 명세: 모든 예외처리/Validation 코드 필수
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 발생 시각", example = "2025-08-04T16:45:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private Integer status;

    @Schema(description = "에러 유형", example = "Not Found")
    private String error;

    @Schema(description = "에러 메시지", example = "해당 aiId의 AI 봇 사용자를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "요청 경로", example = "/api/ai-bots/1/account")
    private String path;

    @Schema(description = "Validation 에러 세부사항")
    private Map<String, String> validationErrors;
}
