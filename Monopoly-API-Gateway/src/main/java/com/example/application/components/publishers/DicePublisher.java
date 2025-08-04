package com.example.application.components.publishers;

import com.example.application.redis.RedisService_Mono;
import com.example.application.types.DicePosition;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.application.config.GameConfig.DICE_UPDATE_CHANNEL;

@Component
@Slf4j
@RequiredArgsConstructor
public class DicePublisher {
    private final Map<String, Sinks.Many<DicePosition>> sinksMap = new ConcurrentHashMap<>();
    private final RedisService_Mono redisService;
    private volatile boolean isActive = true;

    @PostConstruct
    public void init() {
        redisService.listenToChannel_Object(DICE_UPDATE_CHANNEL, DicePosition.class)
                .doOnNext(this::publish)
                .doOnError(e -> log.error("Redis subscription error", e))
                .subscribe();
    }

    public void publish(DicePosition dicePosition) {
        var gameId = dicePosition.getGameId();
        sinksMap.compute(gameId, (_, sink) -> {
            if (sink == null) {
                log.debug("No subscribers for dice updates in game {}", gameId);
                return null;
            }

            Sinks.EmitResult result = sink.tryEmitNext(dicePosition);
            if (result.isFailure()) {
                log.warn("Failed to emit dice position to game {}: {}", gameId, result);
                return null;
            }
            return sink;
        });
    }

    public Publisher<DicePosition> getPublisherForDice(String gameId) {
        return sinksMap.computeIfAbsent(gameId, _ ->
                        Sinks.many()
                                .replay()
                                .limit(1, Duration.ofMinutes(10)))
                .asFlux()
                .doOnCancel(() -> cleanupSink(gameId))
                .doOnTerminate(() -> cleanupSink(gameId));
    }


    private void cleanupSink(String gameId) {
        if (!isActive) return;

        Sinks.Many<DicePosition> sink = sinksMap.get(gameId);
        if (sink != null && sink.currentSubscriberCount() == 0) {
            sinksMap.remove(gameId, sink);
            log.debug("Cleaned up dice sink for game {}", gameId);
        }
    }

    @PreDestroy
    public void cleanup() {
        isActive = false;
        sinksMap.forEach((gameId, sink) -> {
            sink.tryEmitComplete();
            log.debug("Completed dice sink for game {}", gameId);
        });
        sinksMap.clear();
    }
}