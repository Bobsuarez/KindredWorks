package com.kinforgework.cplaneta.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class TemplateResolverUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<String> loadFromRedisOrFile(String redisKey, Resource resource, String jsonField, List<String> fallback) {
        List<String> redisTemplates = loadFromRedis(redisKey);
        if (!redisTemplates.isEmpty()) {
            return redisTemplates;
        }

        List<String> fileTemplates = loadFromJson(resource, jsonField);
        if (!fileTemplates.isEmpty()) {
            saveToRedis(redisKey, fileTemplates);
            return fileTemplates;
        }

        return fallback;
    }

    public String randomOne(List<String> templates) {
        return templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
    }

    private List<String> loadFromRedis(String redisKey) {
        try {
            Long size = redisTemplate.opsForList().size(redisKey);
            if (size == null || size == 0) {
                return Collections.emptyList();
            }

            List<String> values = redisTemplate.opsForList().range(redisKey, 0, -1);
            if (values == null || values.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> loaded = new ArrayList<>();
            values.forEach(value -> {
                if (value != null && !value.isBlank()) {
                    loaded.add(value);
                }
            });
            return loaded;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private List<String> loadFromJson(Resource resource, String jsonField) {
        try {
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            JsonNode node = root.get(jsonField);
            if (node == null || !node.isArray() || node.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> loaded = new ArrayList<>();
            node.forEach(item -> {
                if (item.isTextual() && !item.asText().isBlank()) {
                    loaded.add(item.asText());
                }
            });
            return loaded;
        } catch (IOException ignored) {
            return Collections.emptyList();
        }
    }

    private void saveToRedis(String redisKey, List<String> values) {
        try {
            redisTemplate.delete(redisKey);
            if (!values.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(redisKey, values);
            }
        } catch (Exception ignored) {
        }
    }
}
