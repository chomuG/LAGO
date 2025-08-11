package com.example.LAGO.config;

import com.example.LAGO.constants.ChallengeInterval;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToChallengeIntervalConverter implements Converter<String, ChallengeInterval> {

    @Override
    public ChallengeInterval convert(String source) {
        return ChallengeInterval.fromCode(source);
    }
}
