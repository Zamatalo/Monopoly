package com.example.application.services;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.repo.GameRepo;
import com.example.application.util.enums.GameActions;
import com.example.application.util.enums.GameState;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GameService {
    private final GameRepo gameRepo;

    @PostConstruct
    public void init() {
        gameRepo.save(new Game());
    }


    @Transactional(readOnly = true)
    public List<Game> findAll() {
        return gameRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Game> findById(UUID id) {
        return gameRepo.findById(id);
    }

    @Transactional
    public Game save(Game game) {
        return gameRepo.save(game);
    }

    @Transactional
    public void addPlayerToGame(Player player, Game game) {
        game.addPlayer(player);
        gameRepo.save(game);
    }

    @Transactional(readOnly = true)
    public Optional<Game> findGameByPlayerId(UUID playerId) {
        return gameRepo.findGameByPlayerId(playerId);
    }


    @Transactional(readOnly = true)
    public List<GameActions> resolveGameActions(UUID gameId) {
        var game = gameRepo.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));

        var actions = new ArrayList<GameActions>();

        //the game isn't started
        if (game.getGameState() == GameState.STARTED && game.getPlayers().size() == 4) {
            actions.add(GameActions.START_GAME);
        }
        //possible to start and end timer
        if (game.getGameState() == GameState.IN_PROGRESS) {
            actions.add(game.isTimerRunning() ? GameActions.END_TIMER : GameActions.START_TIMER);
        }
        //possible to end game
        if (game.getGameState() == GameState.FINISHED) {
            actions.add(GameActions.END_GAME);
        }

        return actions;
    }

}
