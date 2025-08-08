package com.example.application.components.publishers;

import com.example.application.redis.RedisService_Mono;
import com.example.application.types.GameDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.application.config.GameConfig.GAME_UPDATE_CHANNEL;

/**
 * Publisher component that listens to Redis channel for game updates
 * and publishes them to reactive subscribers using Reactor {@link Sinks.Many}.
 * <p>
 * Maintains a map of sinks per gameId to support multiple independent streams
 * to clients interested in updates of specific games.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GamePublisher {
    private final Map<String, Sinks.Many<GameDTO>> sinksMap = new ConcurrentHashMap<>();
    private final RedisService_Mono redisService;
    private volatile boolean isActive = true;

    @PostConstruct
    public void init() {
        redisService.listenToChannel_Object(GAME_UPDATE_CHANNEL, GameDTO.class)
                .doOnNext(this::publish)
                .onErrorContinue((throwable, _) -> log.error("Error processing message: {}", throwable.getMessage()))
                .subscribe();
    }


    public void publish(GameDTO gameDTO) {
        if (gameDTO == null || gameDTO.getGameId() == null) {
            log.warn("Invalid gameDTO: {}", gameDTO);
            return;
        }

        sinksMap.compute(gameDTO.getGameId(), (_, sink) -> {
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
        return sinksMap.computeIfAbsent(gameId, _ -> Sinks.many()
                    .replay()
                    .limit(1, Duration.ofMinutes(10)))
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