package com.example.application.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public record RequestContextRedis(
        String correlationId,
        Map<String, String> body,
        MapRecord<String, String, String> rawRecord,
        RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper
) {
    public void respond(Object payloadObject) {
        try {
            String payload = objectMapper.writeValueAsString(payloadObject);

            Map<String, String> response = new HashMap<>();
            response.put("correlationId", correlationId);
            response.put("payload", payload);

            redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .in("game.response")
                            .ofMap(response)
            );
            log.info("Responding to redis for {} {}", correlationId, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to respond via Redis", e);
        }
    }
}


