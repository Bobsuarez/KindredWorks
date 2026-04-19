package com.kinforgework.cplaneta.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisPublisherUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publisherJob(UUID jobId, String filename) {
        Map<String, String> mensaje = Map.of(
                "job_id", jobId.toString(),
                "filename", filename
        );
        String json = objectMapper.writeValueAsString(mensaje);
        redisTemplate.opsForList().leftPush("etl_jobs", json);
    }
}