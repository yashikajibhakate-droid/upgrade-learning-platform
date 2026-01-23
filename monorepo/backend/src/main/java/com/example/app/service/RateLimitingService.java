package com.example.app.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    public ConsumptionProbe resolveBucket(String key) {
        // In a distributed environment (multiple instances), you should use a
        // distributed cache like Redis.
        // For example, with Bucket4j-JCache or Bucket4j-Redis:
        // ProxyManager<String> proxyManager = ...; // configured with Redis
        // Bucket bucket = proxyManager.builder().build(key,
        // this::createNewBucketConfiguration);

        Bucket bucket = buckets.get(key, this::createNewBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createNewBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(1).refillGreedy(1, Duration.ofSeconds(60)).build()) // 1 per min
                .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofHours(1)).build()) // 5 per hour
                .addLimit(Bandwidth.builder().capacity(20).refillGreedy(20, Duration.ofDays(1)).build()) // 20 per day
                .build();
    }
}
