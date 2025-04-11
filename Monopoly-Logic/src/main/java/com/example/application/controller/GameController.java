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
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO exceptions
@Slf4j
@Controller
public class GameController {
    private final GameService gameService;
    private FluxSink<GameDTO> gamesSink;
    private Random random = new Random();
    private ConnectableFlux<GameDTO> gamesPublisher;

    public GameController(GameService gameService) {
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

    @QueryMapping
    public GameDTO findGameById(@Argument("id") String id) {
        Optional<Game> game = gameService.findById(UUID.fromString(id));
        assert game.isPresent();

        return GameMapper.INSTANCE.GameToGameDTO(game.get());
    }

    @QueryMapping
    public List<GameDTO> findAllGames() {
        return gameService.findAll().stream().map(GameMapper.INSTANCE::GameToGameDTO).collect(Collectors.toList());
    }

    @SubscriptionMapping
    public Publisher<GameDTO> gameUpdated(@Argument("gameId") String gameId) {
        return gamesPublisher.filter(gameDto -> gameDto.getGameId().equals(gameId));
    }

    @MutationMapping
    public Game rollDice(@Argument("gameId") String gameId,
                         @Argument("playerId") String playerId) {
        Optional<Game> gameOptional = gameService.findById(UUID.fromString(gameId));
        if (gameOptional.isEmpty()) {
            log.error("Game with id {} not found", gameId);
        }

        Game game = gameOptional.get();
        GameDTO gameDto = GameMapper.INSTANCE.GameToGameDTO(game);
        float randomNumber = random.nextInt(1, 7);

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
