package com.example.application.services;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.repo.GameRepo;
import com.example.application.types.GameDTO;
import com.example.application.util.enums.GameActions;
import com.example.application.util.enums.GameState;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GameService {
    private final GameRepo gameRepo;

    @Transactional(readOnly = true)
    public List<GameDTO> findAll() {
        return gameRepo.findAll().stream().map(GameMapper.INSTANCE::GameToGameDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<GameDTO> findById(UUID id) {
        return gameRepo.findById(id).map(GameMapper.INSTANCE::GameToGameDTO);
    }

    @Transactional
    public GameDTO save(Game game) {
        var gameSaved = gameRepo.save(game);
        return GameMapper.INSTANCE.GameToGameDTO(gameSaved);
    }

    @Transactional
    public void addPlayerToGame(Player player, UUID gameId) {
        var game = gameRepo.findById(gameId).get();
        game.addPlayer(player);
        gameRepo.save(game);
    }

    @Transactional
    public GameDTO startGame(UUID gameId) {
        Game game = gameRepo.findById(gameId).get();

        if (game.getGameState() != GameState.STARTED && game.getPlayers().size() != 4) {
            throw new IllegalStateException("Game cannot be started");
        }
        Game savedGame = gameRepo.save(game);
        return GameMapper.INSTANCE.GameToGameDTO(savedGame);
    }

    @Transactional
    public List<GameActions> resolveGameActions(UUID gameId) {
        Game game = gameRepo.findById(gameId).get();

        List<GameActions> actions = new ArrayList<>();

        // The Game could be started
        if (game.getGameState() == GameState.STARTED && game.getPlayers().size() == 4) {
            actions.add(GameActions.START_GAME);
        } else {
            actions.add(GameActions.JOIN_TO_GAME);
            actions.add(GameActions.ADD_BOT);
        }

        // Timer could be started/ended
        if (game.getGameState() == GameState.IN_PROGRESS) {
            if (game.isTimerRunning()) {
                actions.add(GameActions.END_TIMER);
            } else {
                actions.add(GameActions.START_TIMER);
            }
        }

        // possible to end game
        if (game.getGameState() == GameState.FINISHED) {
            actions.add(GameActions.END_GAME);
        }

        return actions;
    }

}
