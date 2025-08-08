package com.example.application.services.reactive;

import com.example.application.components.GameActionResolver;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.repo.GameRepo;
import com.example.application.types.GameDTO;
import com.example.application.types.GameState;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GameService_Mono {
    private final GameRepo gameRepo;

    @Transactional(readOnly = true)
    public Mono<List<GameDTO>> findAll_Mono() {
        return Mono.fromCallable(() -> {
            List<Game> games = gameRepo.findAll();
            return games.stream().map(this::toEnrichedGameDTO).toList();
        }).subscribeOn(Schedulers.boundedElastic());

    }

    @Transactional(readOnly = true)
    public Mono<GameDTO> findById_Mono(UUID id) {
        return Mono.fromCallable(() -> {
            var game = gameRepo.findById(id)
                    .orElseThrow(EntityNotFoundException::new);
            return toEnrichedGameDTO(game);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<GameDTO> save_Mono(Game game) {
        return Mono.fromCallable(() -> {
            Game savedGame = gameRepo.save(game);
            return toEnrichedGameDTO(savedGame);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<GameDTO> addPlayerToGame_Mono(Player player, UUID gameId) {
        return Mono.fromCallable(() -> {
                    Game game = gameRepo.findById(gameId)
                            .orElseThrow(EntityNotFoundException::new);
                    game.addPlayer(player);
                    return game;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::save_Mono);
    }

    @Transactional
    public Mono<GameDTO> startGame_Mono(UUID gameId) {
        return Mono.fromCallable(() -> {
            Game game = gameRepo.findById(gameId).orElseThrow();

            if (game.getGameState() != GameState.STARTED || game.getPlayers().size() != 4) {
                throw new IllegalStateException("Game cannot be started");
            }

            game.setGameState(GameState.IN_PROGRESS);
            Game savedGame = gameRepo.save(game);
            return toEnrichedGameDTO(savedGame);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<GameDTO> findGameByPlayerId_Mono(UUID playerId) {
        return Mono.fromCallable(() ->
                        gameRepo.findGameByPlayerId(playerId)
                                .map(this::toEnrichedGameDTO)
                                .orElse(null))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<List<PropertyData>> findAllProperties_Mono(UUID gameId) {
        return Mono.fromCallable(() -> {
            Game game = gameRepo.findById(gameId)
                    .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + gameId));
            List<PropertyData> allProperties = new ArrayList<>();

            for (Player player : game.getPlayers()) {
                allProperties.addAll(player.getOwnedProperties());
            }

            return allProperties;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Converts a Game entity into an enriched GameDTO that includes
     * the available game-level and player-level actions.
     *
     * @param game the Game entity
     * @return enriched GameDTO
     */
    private GameDTO toEnrichedGameDTO(Game game) {
        GameDTO dto = GameMapper.INSTANCE.GameToGameDTO(game);
        dto.setGameActions(GameActionResolver.resolveGameActions(dto));

        dto.getPlayers().forEach(playerDTO -> {
            var actions = GameActionResolver.resolvePlayerActions(dto, playerDTO);
            playerDTO.setPlayerActions(actions);
        });

        return dto;
    }

}
