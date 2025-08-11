package com.example.LAGO.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ChallengeInterval {
    MINUTE("1m"),
    MINUTE5("5m"),
    MINUTE10("10m"),
    MINUTE30("30m"),
    HOUR("1h"),
    DAY("1D"),
    WEEK("1W"),
    MONTH("1M"),
    YEAR("1Y")
    ;

    private final String code;

    @JsonCreator
    public static ChallengeInterval fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Stream.of(ChallengeInterval.values())
                .filter(i -> i.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 간격 코드입니다: " + code));
    }
}
