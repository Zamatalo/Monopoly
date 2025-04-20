package com.example.application.controller;

import com.example.application.components.GamePublisher;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/// TODO: add proper error and exception handling
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameSubscription {
    private final GameService gameService;
    private final GamePublisher gamePublisher;
    private final PlayerService playerService;
    private Random random = new Random();


    @SubscriptionMapping
    public Publisher<GameDTO> gameUpdated(@Argument("gameId") String gameId) {
        return gamePublisher.getPublisherForGame(gameId);
    }

    @MutationMapping
    public Integer rollDice(@Argument("gameId") UUID gameId, @Argument("playerId") UUID playerId) {
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> {
                    log.error("Game with id {} not found", gameId);
                    return new IllegalArgumentException("Game not found");
                });

        Player player = playerService.findPlayer(playerId)
                .orElseThrow(() -> {
                    log.error("Player with id {} not found", playerId);
                    return new IllegalArgumentException("Player not found");
                });

        if (game.getPlayers().isEmpty()) {
            throw new IllegalStateException("No players in the game");
        }

        Player current = game.getPlayers().get(game.getCurrentPlayerIndex());

        if (!player.getPlayerId().equals(current.getPlayerId())) {
            throw new IllegalStateException("Wrong player id");
        }

        int diceRoll = random.nextInt(1, 7);

        int newPosition = (current.getPosition() + diceRoll) % 40;
        current.setPosition(newPosition);

        game.setCurrentPlayerIndex((game.getCurrentPlayerIndex() + 1)% game.getPlayers().size());

        gameService.save(game);

        GameDTO updatedDto = GameMapper.INSTANCE.GameToGameDTO(game);
        gamePublisher.publish(updatedDto);

        return diceRoll;
    }

}