//package com.example.application.services.reactive;
//
//import com.example.application.components.GameActionResolver;
//import com.example.application.types.GameDTO;
//import com.example.application.types.PlayerActions;
//import com.example.application.types.PlayerDTO;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.stream.MapRecord;
//import org.springframework.data.redis.connection.stream.StreamRecords;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.util.retry.Retry;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class BotService_Mono {
//    private static final Duration BOT_ACTION_DELAY = Duration.ofSeconds(3);
//    private static final int MAX_RETRY_ATTEMPTS = 3;
//
//    private final GameService_Mono gameService;
//    private final RedisService_Mono redisService;
//    private final Random random = new Random();
//
//    @Scheduled(fixedRate = 60000)
//    @PostConstruct
//    public Mono<Void> checkForStuckBotTurns() {
//        return gameService.findAll_Mono()
//                .flatMapMany(Flux::fromIterable)
//                .filter(game -> game.getPlayers().stream().anyMatch(PlayerDTO::getIsBot))
//                .flatMap(this::handleStuckBotGame)
//                .then();
//    }
//
//    private Mono<Void> handleStuckBotGame(GameDTO game) {
//        return Flux.fromIterable(game.getPlayers())
//                .filter(PlayerDTO::getIsBot)
//                .next()
//                .flatMap(bot -> handelAfterRollActionReactive(game))
//                .onErrorResume(e -> {
//                    log.error("Failed to handle stuck bot turn for game {}", game.getGameId(), e);
//                    return Mono.empty();
//                });
//    }
//
//    public Mono<Void> startBotTurn(UUID gameId) {
//        return gameService.findById_Mono(gameId)
//                .delayElement(BOT_ACTION_DELAY)
//                .flatMap(this::handleBotTurn)
//                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, BOT_ACTION_DELAY))
//                .onErrorResume(e -> {
//                    log.error("Failed to start bot turn for game {}", gameId, e);
//                    return Mono.empty();
//                });
//    }
//
//    private Mono<Void> handleBotTurn(GameDTO game) {
//        PlayerDTO botPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
//        return rollDice(game, botPlayer);
//    }
//
//    private Mono<Void> rollDice(GameDTO game, PlayerDTO botPlayer) {
//        MapRecord<String, String, String> record = createActionRecord(
//                game, botPlayer, "ROLL_DICE");
//
//        return redisService.requestToBackend(record)
//                .doOnSuccess(id -> log.debug("Dice roll initiated for game {} by bot {}",
//                        game.getGameId(), botPlayer.getPlayerId()))
//                .then();
//    }
//
//    private Mono<Void> handelAfterRollActionReactive(GameDTO game) {
//        return Mono.just(game)
//                .delayElement(BOT_ACTION_DELAY)
//                .flatMap(g -> {
//                    PlayerDTO botPlayer = g.getPlayers().get(g.getCurrentPlayerIndex());
//                    List<PlayerActions> actions = GameActionResolver.resolvePlayerActions(g, botPlayer);
//                    if (!actions.isEmpty()) {
//                        PlayerActions chosen = actions.get(random.nextInt(actions.size()));
//                        return executeActionReactive(g, botPlayer, chosen);
//                    }
//                    return Mono.empty();
//                });
//    }
//
//    private Mono<Void> executeActionReactive(GameDTO game, PlayerDTO botPlayer, PlayerActions action) {
//        MapRecord<String, String, String> record = createActionRecord(
//                game, botPlayer, action.toString());
//
//        return redisService.answerToGateway(record)
//                .doOnSuccess(id -> log.debug("Action {} executed for game {}, recordId: {}",
//                        action, game.getGameId(), id))
//                .onErrorResume(e -> {
//                    log.error("Failed to execute action {} for game {}",
//                            action, game.getGameId(), e);
//                    return Mono.empty();
//                })
//                .then();
//    }
//
//    private MapRecord<String, String, String> createActionRecord(
//            GameDTO game, PlayerDTO player, String action) {
//        Map<String, String> data = Map.of(
//                "action", action,
//                "gameId", game.getGameId(),
//                "playerId", player.getPlayerId(),
//                "correlationId", UUID.randomUUID().toString(),
//                "sentFromBot", "true"
//        );
//        return StreamRecords.string(data).withStreamKey("game.request");
//    }
//}