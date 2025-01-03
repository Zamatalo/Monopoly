package com.example.backend.service;

import com.example.backend.repostitory.GameRepository;
import com.example.shared.GameState;
import com.example.shared.exeption.GameNotFoundException;
import com.example.shared.exeption.PlayerNotFoundException;
import com.example.shared.model.game.Game;
import com.example.shared.model.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void startNewGame(List<String> playerNames) {
        Game game = new Game();
        List<Player> players = new ArrayList<>();

        playerNames.forEach(playerName -> {
            playerName=playerName.toLowerCase();
            Player player = Player.builder()
                    .name(playerName)
                    .build();
            players.add(player);
        });

        game.setPlayers(players);
        game.setGameState(GameState.IN_PROGRESS);
        gameRepository.save(game);
    }

    public void movePlayer(UUID gameId, UUID playerId, int steps) {

        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isPresent()) {
            Game game = gameOpt.get();
            Optional<Player> playerOpt = game.getPlayers().stream()
                    .filter(p -> p.getId().equals(playerId))
                    .findFirst();
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                int newPosition = (player.getPosition() + steps) % 40;
                player.setPosition(newPosition);

                log.info("Move player {} to game {}", playerId, gameId);
                gameRepository.save(game);
                return;
            }
            throw new PlayerNotFoundException(playerId);
        }
        throw new GameNotFoundException(gameId);
    }
}
