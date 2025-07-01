package com.example.application.services;

import com.example.application.components.GameActionResolver;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BotService {
    private final RedisTemplate<String, String> redisTemplate;
    private final GameService gameService;
    private final Random random = new Random();

    @Scheduled(fixedRate = 60000)
    @PostConstruct
    public void checkForStuckBotTurns() {
        List<GameDTO> games = gameService.findAll();
        for (GameDTO game : games) {
            List<PlayerDTO> currentPlayer = game.getPlayers();
            currentPlayer.forEach(player -> {
                if (player.getIsBot()) {
                    handelAfterRollAction(game);
                }
            });
        }
    }

    public void startBotTurn(UUID gameId) {
        GameDTO game = gameService.findById(gameId);
        PlayerDTO botPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());

        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                .execute(() -> rollDice(game, botPlayer));
    }

    private void rollDice(GameDTO game, PlayerDTO botPlayer) {
        Map<String, String> action = Map.of(
                "action", "ROLL_DICE",
                "gameId", game.getGameId().toString(),
                "playerId", botPlayer.getPlayerId().toString(),
                "correlationId", UUID.randomUUID().toString(),
                "sentFromBot", "true"
        );

        redisTemplate.opsForStream().add(
                StreamRecords.string(action).withStreamKey("game.request")
        );
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
        Map<String, String> actionBody = Map.of(
                "action", action.toString(),
                "gameId", game.getGameId().toString(),
                "playerId", botPlayer.getPlayerId().toString(),
                "correlationId", UUID.randomUUID().toString(),
                "sentFromBot", "true"
        );

        redisTemplate.opsForStream().add(
                StreamRecords.string(actionBody).withStreamKey("game.request")
        );
    }
}