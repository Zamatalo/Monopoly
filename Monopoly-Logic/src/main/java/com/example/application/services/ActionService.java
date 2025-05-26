package com.example.application.services;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.util.PropertyData;
import com.example.application.util.enums.GameActions;
import com.example.application.util.enums.GameState;
import com.example.application.util.enums.PlayerActions;
import com.example.application.util.enums.PlayerState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final GameService gameService;
    private final PlayerService playerService;

    public List<GameActions> resolveGameActions(UUID gameId) {
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found"));

        List<GameActions> actions = new ArrayList<>();

        // The Game could be started
        if (game.getGameState() == GameState.STARTED && game.getPlayers().size() == 4) {
            actions.add(GameActions.START_GAME);
        }else{
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

    public List<PlayerActions> resolvePlayerActions(UUID playerId) {
        Player player = playerService.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("Player not found"));

        Game game = player.getGame();

        List<PlayerActions> actions = new ArrayList<>();
        //the game isn't started
        if (game.getGameState() != GameState.IN_PROGRESS) {
            return actions;
        }

        //not a player's turn
        if (!game.getCurrentPlayer().getPlayerId().equals(playerId)) {
            return actions;
        }

        // Waiting for player response
        if (player.getPlayerState() == PlayerState.IDLE) {
            actions.add(PlayerActions.ROLL_DICE);
        }

        // player moved
        if (player.getPlayerState() == PlayerState.MOVED) {
            if (canBuy(player.getPosition(), game)) {
                actions.add(PlayerActions.BUY_PROPERTY);
            }
            actions.add(PlayerActions.END_TURN);
        }

        return actions;
    }

    private boolean canBuy(int position, Game game) {
        return game.getPlayers().stream()
                .flatMap(p -> p.getOwnedProperties().stream())
                .map(PropertyData::boardPosition)
                .noneMatch(pos -> pos == position);
    }

}
