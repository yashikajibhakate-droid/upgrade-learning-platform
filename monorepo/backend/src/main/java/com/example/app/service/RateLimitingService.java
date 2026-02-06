package com.example.app.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {

  private final Cache<String, Bucket> buckets =
      Caffeine.newBuilder().maximumSize(1000).expireAfterAccess(1, TimeUnit.HOURS).build();

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
        .addLimit(
            Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofSeconds(60))
                .build()) // 5 per min
        .addLimit(
            Bandwidth.builder()
                .capacity(20)
                .refillGreedy(20, Duration.ofHours(1))
                .build()) // 20 per hour
        .addLimit(
            Bandwidth.builder()
                .capacity(50)
                .refillGreedy(50, Duration.ofDays(1))
                .build()) // 50 per day
        .build();
  }
}
