package com.example.application.services.reactive;


import com.example.application.components.GameActionResolver;
import com.example.application.redis.RedisService_Mono;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.example.application.config.GameConfig.*;

/**
 * Service managing automated bot players within games.
 * <p>
 * Listens to game updates and triggers bot actions on their turns
 * with a configurable delay, simulating bot behavior.
 * Periodically checks for stuck bot turns and attempts to resolve them.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotService_Mono {
    private static final Duration BOT_ACTION_DELAY = Duration.ofSeconds(7);

    private final GameService_Mono gameService;
    private final RedisService_Mono redisService;
    private final Random random = new Random();

    private Disposable subscribe;

    @PostConstruct
    public void init(){
        subscribe = redisService
                .listenToChannel_Object(GAME_UPDATE_CHANNEL,GameDTO.class)
                .flatMap(e-> {
                    if(e.getPlayers().get(e.getCurrentPlayerIndex()).getIsBot()){
                        return handleBotTurn(e);
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("Failed to start bot turn {}",e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    @Scheduled(fixedRate = 10000)
    public void checkForStuckBotTurns() {
        gameService.findAll_Mono()
                .flatMapMany(Flux::fromIterable)
                .filter(game -> game.getPlayers().get(game.getCurrentPlayerIndex()).getIsBot())
                .flatMap(this::handleBotTurn)
                .subscribe();
    }

    public Mono<Void> handleBotTurn(GameDTO game) {
        return Mono.just(game)
                .delaySubscription(BOT_ACTION_DELAY)
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

        return redisService.publishToStream(REQUEST_STREAM,request)
                .doOnSuccess(id -> log.debug("Action {} executed for game {}, recordId: {}",
                        action, game.getGameId(), id))
                .onErrorResume(e -> {
                    log.error("Failed to execute action {} for game {}",
                            action, game.getGameId(), e);
                    return Mono.empty();
                })
                .then();
    }

    @PreDestroy
    public void cleanup() {
        if (subscribe != null && !subscribe.isDisposed()) {
            subscribe.dispose();
            log.info("Stopped listening to responses stream");
        }
    }
}
