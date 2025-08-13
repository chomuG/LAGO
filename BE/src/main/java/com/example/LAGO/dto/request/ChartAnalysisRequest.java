package com.example.LAGO.dto.request;

import com.example.LAGO.constants.Interval;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChartAnalysisRequest {

    @NotNull(message = "주식 ID는 필수입니다.")
    private Long stockId;

    @NotNull(message = "조회 간격(1m/1D/1M/1Y)은 필수입니다.")
    private Interval interval;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;
}
