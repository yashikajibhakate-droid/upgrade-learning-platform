package com.example.app.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ModerationService {

    private static final List<String> BANNED_WORDS = Arrays.asList("spam", "abuse", "offensive");

    public boolean isClean(String content) {
        if (content == null || content.isEmpty()) {
            return true;
        }
        String lowerCaseContent = content.toLowerCase();
        for (String word : BANNED_WORDS) {
            if (lowerCaseContent.contains(word)) {
                return false;
            }
        }
        return true;
    }
}
