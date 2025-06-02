package com.example.application.components.publishers;

import com.example.application.types.GameDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class GamePublisher {
    private final Map<String, Sinks.Many<GameDTO>> sinksMap = new ConcurrentHashMap<>();
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private volatile boolean isActive = true;

    @PostConstruct
    public void init() {
        redisTemplate.listenTo(ChannelTopic.of("game:gameUpdate"))
                //.cast(GameDTO.class)
                .doOnNext(stringObjectMessage ->
                        publish((GameDTO) stringObjectMessage.getMessage()))
                .doOnError(e -> log.error("Redis subscription error", e))
                .subscribe();
    }


    public void publish(GameDTO gameDTO) {
        if (gameDTO == null || gameDTO.getGameId() == null) {
            log.warn("Invalid gameDTO: {}", gameDTO);
            return;
        }

        sinksMap.compute(gameDTO.getGameId(), (key, sink) -> {
            if (sink == null) {
                log.debug("No subscribers for game {}", gameDTO.getGameId());
                return null;
            }

            Sinks.EmitResult result = sink.tryEmitNext(gameDTO);
            if (result.isFailure()) {
                log.warn("Failed to emit to sink for game {}: {}", gameDTO.getGameId(), result);
                return null;
            }
            return sink;
        });
    }

    public Flux<GameDTO> getPublisherForGame(String gameId) {
        return sinksMap.computeIfAbsent(gameId, _ ->
                        Sinks.many()
                                .replay()
                                .<GameDTO>limit(1, Duration.ofMinutes(10)))
                .asFlux()
                .doOnCancel(() -> cleanupSink(gameId))
                .doOnTerminate(() -> cleanupSink(gameId));
    }

    private void cleanupSink(String gameId) {
        if (!isActive) return;

        Sinks.Many<GameDTO> sink = sinksMap.get(gameId);
        if (sink != null && sink.currentSubscriberCount() == 0) {
            sinksMap.remove(gameId, sink);
            log.debug("Cleaned up sink for game {}", gameId);
        }
    }

    @PreDestroy
    public void cleanup() {
        isActive = false;

        sinksMap.forEach((gameId, sink) -> {
            sink.tryEmitComplete();
            log.debug("Completed sink for game {}", gameId);
        });
        sinksMap.clear();
    }
}