package com.example.application.components.publishers;

import com.example.application.types.GameDTO;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Publisher for GraphQL subscriptions
@Component
@Slf4j
public class GamePublisher {
    private final Map<String, Sinks.Many<GameDTO>> sinksMap = new ConcurrentHashMap<>();

    public void publish(GameDTO gameDTO) {
        String gameId = gameDTO.getGameId();

        if (gameId == null) {
            log.warn("Attempted to publish to null gameId. Event: {}");
            return;
        }

        Sinks.Many<GameDTO> sink = sinksMap.get(gameId);
        if (sink != null) {
            Sinks.EmitResult result = sink.tryEmitNext(gameDTO);
            if (result.isFailure()) {
                System.err.println("Failed to emit update to sink: " + result);
                sinksMap.remove(gameDTO.getGameId(), sink);
            }
        }

    }

    public Publisher<GameDTO> getPublisherForGame(String gameId) {
        return sinksMap.computeIfAbsent(gameId, _ ->
                Sinks.many().replay().latest()).asFlux();
    }

    @PreDestroy
    public void cleanup() {
        sinksMap.values().forEach(Sinks.Many::tryEmitComplete);
        sinksMap.clear();
    }
}