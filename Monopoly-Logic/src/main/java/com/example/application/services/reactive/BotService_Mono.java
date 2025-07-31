package com.example.application.services.reactive;

import com.example.application.components.GameActionResolver;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService_Mono {
    private static final Duration BOT_ACTION_DELAY = Duration.ofSeconds(3);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final GameService_Mono gameService;
    private final RedisService_Mono redisService;
    private final Random random = new Random();

    @Scheduled(fixedRate = 1000)
    public void checkForStuckBotTurns() {
        gameService.findAll_Mono()
                .flatMapMany(Flux::fromIterable)
                .filter(game -> game.getPlayers().get(game.getCurrentPlayerIndex()).getIsBot())
                .flatMap(this::handleStuckBotGame)
                .subscribe();
    }

    private Mono<Void> handleStuckBotGame(GameDTO game) {
        return Flux.fromIterable(game.getPlayers())
                .filter(PlayerDTO::getIsBot)
                .next()
                .flatMap(_ -> handleAfterRollAction(game))
                .onErrorResume(e -> {
                    log.error("Failed to handle stuck bot turn for game {}", game.getGameId(), e);
                    return Mono.empty();
                });
    }

    public Mono<Void> startBotTurn(UUID gameId) {
        return gameService.findById_Mono(gameId)
                .delayElement(BOT_ACTION_DELAY)
                .flatMap(this::handleBotTurn)
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, BOT_ACTION_DELAY))
                .onErrorResume(e -> {
                    log.error("Failed to start bot turn for game {}", gameId, e);
                    return Mono.empty();
                });
    }

    /// First always should be roll dice, then choose a random action from available actions
    /// @see GameActionResolver
    private Mono<Void> handleBotTurn(GameDTO game) {
        PlayerDTO botPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        return executeAction(game, botPlayer, PlayerActions.ROLL_DICE);
    }

    public Mono<Void> handleAfterRollAction(GameDTO game) {
        return Mono.just(game)
                .delayElement(BOT_ACTION_DELAY)
                .flatMap(g -> {
                    PlayerDTO botPlayer = g.getPlayers().get(g.getCurrentPlayerIndex());
                    List<PlayerActions> actions = GameActionResolver.resolvePlayerActions(g, botPlayer);
                    if (!actions.isEmpty()) {
                        PlayerActions chosen = actions.get(random.nextInt(actions.size()));
                        return executeAction(g, botPlayer, chosen);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> executeAction(GameDTO game, PlayerDTO botPlayer, PlayerActions action) {
        Map<String, String> request = new HashMap<>();
        request.put("action", action.name());
        request.put("gameId", game.getGameId());
        request.put("playerId", botPlayer.getPlayerId());
        request.put("sentFromBot", "true");

        return redisService.publishToRequestStream(request)
                .doOnSuccess(id -> log.debug("Action {} executed for game {}, recordId: {}",
                        action, game.getGameId(), id))
                .onErrorResume(e -> {
                    log.error("Failed to execute action {} for game {}",
                            action, game.getGameId(), e);
                    return Mono.empty();
                })
                .then();
    }
}
