package com.example.application.components;

import com.example.application.types.GameDTO;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import com.netflix.graphql.dgs.InputArgument;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@DgsComponent
public class GameSubscription {
    private final Map<String, Sinks.Many<GameDTO>> gameStreams = new ConcurrentHashMap<>();

    public void updateGame(GameDTO game) {
        Sinks.Many<GameDTO> sink = gameStreams.computeIfAbsent(
                game.getGameId(),
                id -> Sinks.many().multicast().onBackpressureBuffer()
        );

        Sinks.EmitResult result = sink.tryEmitNext(game);
        if (result.isFailure()) {
            if (result == Sinks.EmitResult.FAIL_CANCELLED) {
                gameStreams.remove(game.getGameId());
                updateGame(game);
            } else {
                log.error("Failed to emit game update: {}", result);
            }
        }
    }

    @DgsSubscription
    public Publisher<GameDTO> gameUpdated(@InputArgument("gameId") String gameId) {
        return gameStreams.computeIfAbsent(gameId, id -> Sinks.many().multicast().onBackpressureBuffer())
                .asFlux()
                .doOnCancel(() -> {
                    gameStreams.remove(gameId);
                    log.info("Subscription cancelled for game ID: {}", gameId);
                })
                .doOnTerminate(() -> {
                    gameStreams.remove(gameId);
                    log.info("Subscription terminated for game ID: {}", gameId);
                });
    }
}