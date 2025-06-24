package com.example.application.utility;

import com.example.application.util.exceptions.RedisResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public record RequestContextRedis(
        String correlationId,
        Map<String, String> body,
        MapRecord<String, String, String> rawRecord,
        RedisOperations<String, Object> redisOp,
        ObjectMapper objectMapper
) {
    private static final String RESPONSE_STREAM = "game.response";

    public void respond(Object payload) {
        try {
            Map<String, String> body = new HashMap<>();
            String payloadJson = objectMapper.writeValueAsString(payload);
            body.put("payload", payloadJson);
            body.put("correlationId", correlationId);

            MapRecord<String, String, String> record = StreamRecords.newRecord()
                    .ofMap(body)
                    .withStreamKey(RESPONSE_STREAM);


            redisOp.opsForStream().add(record);
            //log.info("Sent response to Redis stream for correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("Failed to send response to Redis for correlationId: {}", correlationId, e);
            throw new RedisResponseException("Failed to send response to Redis", e);
        }
    }
}