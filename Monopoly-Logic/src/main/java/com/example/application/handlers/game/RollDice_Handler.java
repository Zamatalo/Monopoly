package com.example.application.handlers.game;

import com.example.application.services.DiceService;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.services.RedisService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.util.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RollDice_Handler implements GameActionHandler {
    private final DiceService diceService;
    private final GameService gameService;
    private final PlayerService playerService;
    private final RedisService redisService;

    @Override
    public String getAction() {
        return "ROLL_DICE";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
            var gameId = UUID.fromString(ctx.body().get("gameId"));
            var playerId = UUID.fromString(ctx.body().get("playerId"));
            CompletableFuture<Integer> future = diceService.rollDice(gameId.toString());
            var rolledResult = future.join();

            GameDTO game = gameService.findById(gameId);
            PlayerDTO player = playerService.findById(playerId).orElseThrow();

            int newPosition = (player.getPosition() + rolledResult) % 40;
            PropertyData steppedOn = steppedOnAnotherPlayerField(gameId, newPosition);
            if (steppedOn != null) {
                player.setBalance(player.getBalance() - steppedOn.cost());
            }

            player.setPlayerState(PlayerState.AWAITING_DECISION);
            player.setPosition(newPosition);

            List<PlayerDTO> updatedPlayers = game.getPlayers().stream()
                    .map(p -> p.getPlayerId().equals(player.getPlayerId()) ? player : p)
                    .toList();
            game.setPlayers(updatedPlayers);

            gameService.save(GameMapper.INSTANCE.GameDTOtoGame(game));
            redisService.publishGameUpd(gameService.findById(gameId));

            if (!isFromBot(ctx)) {
                ctx.respond(rolledResult);
            }
    }

    private PropertyData steppedOnAnotherPlayerField(UUID gameId, Integer playerPos) {
        var properties = gameService.findAllProperties(gameId);
        return properties.stream()
                .filter(propertyData -> propertyData.boardPosition() == playerPos)
                .findFirst()
                .orElse(null);
    }

    /// if its from bot, record should be ack
    public boolean isFromBot(RequestContextRedis ctx) {
        var a = ctx.body().get("sentFromBot");
        return a != null && a.equals("true");
    }
}
