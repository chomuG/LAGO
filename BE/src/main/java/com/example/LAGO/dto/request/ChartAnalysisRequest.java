package com.example.LAGO.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ChartAnalysisRequest {

    @NotNull(message = "주식 ID는 필수입니다.")
    private Long stockId;

    @NotBlank(message = "조회 간격(분/일/주)은 필수입니다.")
    private String interval; // "minute", "day", "week"

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;
}
