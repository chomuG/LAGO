package com.example.LAGO.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 계좌 정보 DTO (컨트롤러 응답에 사용 금지 아님: 내부 DTO로 매핑/전달 용도)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계좌 정보 DTO")
public class AccountDto {

    @Schema(description = "계좌 ID", example = "101")
    private Integer accountId;

    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;

    @Schema(description = "보유 현금", example = "1500000")
    private Integer balance;

    @Schema(description = "총 자산", example = "5200000")
    private Integer totalAsset;

    @Schema(description = "수익", example = "20000")
    private Integer profit;

    @Schema(description = "수익률(%)", example = "1.25")
    private Float profitRate;

    @Schema(description = "계좌 생성일")
    private LocalDateTime createdAt;

    @Schema(description = "계좌 구분(현시점/역사챌린지 등)", example = "CURRENT")
    private String type;
}
