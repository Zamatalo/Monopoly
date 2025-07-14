package com.example.application.services.reactive;

import com.example.application.components.GameActionResolver;
import com.example.application.handlers.game.RollDice_Handler;
import com.example.application.redis.GameRequestStreamListener;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * on startup checks if there are any stuck bots and handles bot turns.
 * <ui>
 * <li>1.send ROLL_DICE action into {@link  GameRequestStreamListener} and after into  {@link RollDice_Handler}</li>
 * <li>2. the {@link #handelAfterRollAction(GameDTO)} will be called  </li>
 * </ui>
 */
@Service
@RequiredArgsConstructor
public class BotService_Mono {
    private final GameService_Mono gameService;
    private final Random random = new Random();
    private final RedisService_Mono redisService;

    @Scheduled(fixedRate = 60000)
    @PostConstruct
    public void checkForStuckBotTurns() {
        gameService.findAll_Mono()
                .flatMapMany(Flux::fromIterable)
                .flatMap(game ->
                        Flux.fromIterable(game.getPlayers())
                                .filter(PlayerDTO::getIsBot)
                                .doOnNext(bot -> handelAfterRollAction(game))
                )
                .subscribe();
    }

    public Mono<Void> startBotTurn(UUID gameId) {
        return gameService.findById_Mono(gameId)
                .delayElement(Duration.ofSeconds(3))
                .doOnNext(game -> {
                    PlayerDTO botPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
                    rollDice(game, botPlayer);
                })
                .then();
    }

    private void rollDice(GameDTO game, PlayerDTO botPlayer) {
        Map<String, String> data = Map.of(
                "action", "ROLL_DICE",
                "gameId", game.getGameId(),
                "playerId", botPlayer.getPlayerId(),
                "correlationId", UUID.randomUUID().toString(),
                "sentFromBot", "true"
        );

        var stream = StreamRecords.string(data);
        redisService.requestToBackend(stream);
    }

    public void handelAfterRollAction(GameDTO game) {
        PlayerDTO botPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());

        List<PlayerActions> possibleActions = GameActionResolver.resolvePlayerActions(game, botPlayer);
        if (!possibleActions.isEmpty()) {
            PlayerActions chosenAction = possibleActions.get(random.nextInt(possibleActions.size()));
            CompletableFuture.delayedExecutor(4, TimeUnit.SECONDS)
                    .execute(() -> executeAction(game, botPlayer, chosenAction));
        }
    }

    private void executeAction(GameDTO game, PlayerDTO botPlayer, PlayerActions action) {
        Map<String, String> data = Map.of(
                "action", action.toString(),
                "gameId", game.getGameId(),
                "playerId", botPlayer.getPlayerId(),
                "correlationId", UUID.randomUUID().toString(),
                "sentFromBot", "true"
        );

        redisTemplate.opsForStream().add(
                StreamRecords.string(data).withStreamKey("game.request")
        ).subscribe();
    }
}