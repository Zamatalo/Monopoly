package com.example.application.handlers.game;

import com.example.application.services.DiceService;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.services.RedisService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.util.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        try {
            var gameId = UUID.fromString(ctx.body().get("gameId"));
            var playerId = UUID.fromString(ctx.body().get("playerId"));
            CompletableFuture<Integer> future = diceService.rollDice(gameId.toString());
            var rolledResult = future.join();

            GameDTO game = gameService.findById(gameId).orElseThrow();
            PlayerDTO player = playerService.findById(playerId).orElseThrow();

            int newPosition = (player.getPosition() + rolledResult) % 40;
            PropertyData steppedOn = steppedOnAnotherPlayerField(gameId, newPosition);
            if (steppedOn != null) {
                player.setBalance(player.getBalance() - steppedOn.cost());
            }

            int nextPlayer = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();

            game.setCurrentPlayerIndex(nextPlayer);
            player.setPosition(newPosition);

            gameService.save(GameMapper.INSTANCE.GameDTOtoGame(game));
            redisService.publishGameUpd(gameService.findById(gameId).get());
            ctx.respond(rolledResult);
            isNextPlayerBot(game, nextPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PropertyData steppedOnAnotherPlayerField(UUID gameId, Integer playerPos) {
        var properties = gameService.findAllProperties(gameId);
        return properties.stream()
                .filter(propertyData -> propertyData.boardPosition() == playerPos)
                .findFirst()
                .orElse(null);
    }

    /// later should be in BotService with scheduler
    private void isNextPlayerBot(GameDTO game, Integer playerIndex) {
        var nextPlayer = game.getPlayers().get(playerIndex);
        if (nextPlayer.getIsBot()) {
            var rolledResult = diceService.rollDice(game.getGameId()).join();


            int newPosition = (nextPlayer.getPosition() + rolledResult) % 40;
            PropertyData steppedOn = steppedOnAnotherPlayerField(UUID.fromString(game.getGameId()), newPosition);
            if (steppedOn != null) {
                nextPlayer.setBalance(nextPlayer.getBalance() - steppedOn.cost());
            }

            int nextPlayerIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();

            game.setCurrentPlayerIndex(nextPlayerIndex);

            nextPlayer.setPosition(newPosition);
            gameService.save(GameMapper.INSTANCE.GameDTOtoGame(game));
            playerService.savePlayer(GameMapper.INSTANCE.dtoToPlayer(nextPlayer));
            isNextPlayerBot(game, nextPlayerIndex);
        }
    }
}
