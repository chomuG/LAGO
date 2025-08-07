package com.example.LAGO.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChartPatternAnalysisRequestDto {

    @JsonProperty("stock_info_id")
    private int stockInfoId;
}
