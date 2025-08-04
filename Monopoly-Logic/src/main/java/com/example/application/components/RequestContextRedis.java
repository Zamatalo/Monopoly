package com.example.application.components;

import com.example.application.redis.RedisService_Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.application.config.GameConfig.RESPONSE_STREAM;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestContextRedis {
    private final String correlationId;
    @Getter
    private final Map<String, String> body;
    private final RedisService_Mono redisService;
    private final ObjectMapper objectMapper;

    public Mono<Void> respond(Object responseData) {
        if ("true".equals(body.get("sentFromBot"))) {
            return Mono.empty();
        }

        return buildResponseRecord(responseData)
                .flatMap(e -> redisService.publishToStream(RESPONSE_STREAM, e))
                .then();
    }

    private Mono<Map<String, String>> buildResponseRecord(Object responseData) {
        return Mono.fromCallable(() -> {
            String payloadJson = responseData == null ? "" : objectMapper.writeValueAsString(responseData);
            String correlation = correlationId == null ? "" : correlationId;
            return Map.of(
                    "correlationId", correlation,
                    "payload", payloadJson
            );
        });
    }
}
