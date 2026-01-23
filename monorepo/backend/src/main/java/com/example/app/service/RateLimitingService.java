package com.example.app.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public ConsumptionProbe resolveBucket(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, this::createNewBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createNewBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(1).refillGreedy(1, Duration.ofSeconds(60)).build()) // 1 per min
                .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofHours(1)).build())   // 5 per hour
                .addLimit(Bandwidth.builder().capacity(20).refillGreedy(20, Duration.ofDays(1)).build())  // 20 per day
                .build();
    }
}
