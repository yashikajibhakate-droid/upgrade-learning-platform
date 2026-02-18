package com.example.app.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ModerationService {

    private static final List<String> BANNED_WORD_STRINGS = Arrays.asList("spam", "abuse", "offensive");
    private static final List<java.util.regex.Pattern> BANNED_PATTERNS;

    static {
        BANNED_PATTERNS = BANNED_WORD_STRINGS.stream()
                .map(word -> java.util.regex.Pattern.compile("\\b" + java.util.regex.Pattern.quote(word) + "\\b",
                        java.util.regex.Pattern.CASE_INSENSITIVE))
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean isClean(String content) {
        if (content == null || content.isEmpty()) {
            return true;
        }
        for (java.util.regex.Pattern pattern : BANNED_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return false;
            }
        }
        return true;
    }
}
