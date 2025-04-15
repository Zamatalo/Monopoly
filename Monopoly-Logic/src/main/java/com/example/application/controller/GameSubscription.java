package com.example.application.controller;

import com.example.application.entity.Game;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;


@Controller
@Slf4j
public class GameSubscription {

    private final GameService gameService;
    private final Random random = new Random();
    private FluxSink<GameDTO> gamesSink;
    private ConnectableFlux<GameDTO> gamesPublisher;

    public GameSubscription(GameService gameService) {
        this.gameService = gameService;
    }

    @PostConstruct
    public void init() {
        Flux<GameDTO> publisher = Flux.create(sink -> {
            gamesSink = sink;
        });
        gamesPublisher = publisher.publish();
        gamesPublisher.connect();
    }

    @SubscriptionMapping
    public Publisher<GameDTO> gameUpdated(@Argument("gameId") String gameId) {
        return gamesPublisher.filter(gameDto -> gameDto.getGameId().equals(gameId));
    }

    @MutationMapping
    public Game rollDice(@Argument("gameId") String gameId, @Argument("playerId") String playerId) {
        Optional<Game> gameOptional = gameService.findById(UUID.fromString(gameId));
        if (gameOptional.isEmpty()) {
            log.error("Game with id {} not found", gameId);
        }

        float randomNumber = random.nextInt(1, 7);
        Game game = gameOptional.get();
        GameDTO gameDto = GameMapper.INSTANCE.GameToGameDTO(game);

        int currentPlayerIndex = gameDto.getCurrentPlayerIndex();
        int newPosition = (gameDto.getPlayers().get(currentPlayerIndex).getPosition() + Math.round(randomNumber)) % 40;
        gameDto.getPlayers().get(currentPlayerIndex).setPosition(newPosition);

        int numberOfPlayers = gameDto.getPlayers().size();
        gameDto.setCurrentPlayerIndex((currentPlayerIndex + 1) % numberOfPlayers);

        Game updatedGame = GameMapper.INSTANCE.GameDTOtoGame(gameDto);
        gameService.save(updatedGame);
        gamesSink.next(gameDto);
        return game;
    }
}