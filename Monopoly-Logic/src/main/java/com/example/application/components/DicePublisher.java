package com.example.application.components;

import com.example.application.types.DicePosition;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DicePublisher {
    private final Map<String, Sinks.Many<DicePosition>> sinksMap = new ConcurrentHashMap<>();

    public void publish(String gameId, DicePosition dicePosition) {
        Sinks.Many<DicePosition> sink = sinksMap.get(gameId);
        if (sink != null) {
            sink.tryEmitNext(dicePosition);
        }
    }

    public Publisher<DicePosition> getPublisherForDice(String gameId) {
        return sinksMap.computeIfAbsent(gameId, _ -> Sinks.many()
                .replay().latest()).asFlux();
    }

    public Flux<DicePosition> getPublisherForDiceAsFlux(String gameId) {
        return Flux.from(getPublisherForDice(gameId));
    }

    public void removeDicePublisher(String gameId) {
        Sinks.Many<DicePosition> sink = sinksMap.remove(gameId);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    @PreDestroy
    public void cleanup() {
        sinksMap.values().forEach(Sinks.Many::tryEmitComplete);
        sinksMap.clear();
    }
}