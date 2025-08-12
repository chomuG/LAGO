package com.example.LAGO.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Interval {
    MINUTE("1m"),
    DAY("1D"),
    WEEK("1W"),
    MONTH("1M"),
    YEAR("1Y");

    private final String code;

    @JsonCreator
    public static Interval fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Stream.of(Interval.values())
                .filter(i -> i.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 간격 코드입니다: " + code));
    }
}
