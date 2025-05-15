package com.example.application.components;

import com.example.application.types.GameDTO;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Publisher for GraphQL subscriptions
@Component
public class GamePublisher {
    private final Map<String, Sinks.Many<GameDTO>> sinksMap = new ConcurrentHashMap<>();

    public void publish(GameDTO gameDTO) {
        String gameId = gameDTO.getGameId();
        Sinks.Many<GameDTO> sink = sinksMap.get(gameId);
        if (sink != null) {
            sink.tryEmitNext(gameDTO);
        }
    }

    public Publisher<GameDTO> getPublisherForGame(String gameId) {
        return sinksMap.computeIfAbsent(gameId, id ->
                Sinks.many().replay().latest()).asFlux();
    }

    @PreDestroy
    public void cleanup() {
        sinksMap.values().forEach(Sinks.Many::tryEmitComplete);
        sinksMap.clear();
    }
}