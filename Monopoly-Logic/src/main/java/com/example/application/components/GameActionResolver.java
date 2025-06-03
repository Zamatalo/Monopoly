package com.example.application.components;

import com.example.application.types.*;

import java.util.ArrayList;
import java.util.List;

public abstract class GameActionResolver {

    public static List<GameActions> resolveGameActions(GameDTO game) {
        List<GameActions> actions = new ArrayList<>();

        // The Game could be started
        if (game.getGameState() == GameState.STARTED && game.getPlayers().size() == 4) {
            actions.add(GameActions.START_GAME);
        }
        if (game.getGameState() == GameState.STARTED && game.getPlayers().size() < 4) {
            actions.add(GameActions.JOIN_TO_GAME);
            actions.add(GameActions.ADD_BOT);
        }
        if (game.getGameState() == GameState.IN_PROGRESS) {
            ///
        }

        // Timer could be started/ended
//        if (game.getGameState() == GameState.IN_PROGRESS) {
//            if (game.isTimerRunning()) {
//                actions.add(GameActions.END_TIMER);
//            } else {
//                actions.add(GameActions.START_TIMER);
//            }
//        }

        // possible to end game
        if (game.getGameState() == GameState.FINISHED) {
            actions.add(GameActions.END_GAME);
        }

        return actions;
    }

    public static List<PlayerActions> resolvePlayerActions(GameDTO game, PlayerDTO player) {
        List<PlayerActions> actions = new ArrayList<>();
        var currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());

        //the game isn't started
        if (game.getGameState() != GameState.IN_PROGRESS) {
            return actions;
        }

        //not a player's turn
        if (!currentPlayer.getPlayerId().equals(player.getPlayerId())) {
            return actions;
        }

        // Waiting for player response
        if (player.getPlayerState() == PlayerState.IDLE) {
            actions.add(PlayerActions.ROLL_DICE);
        }

        // player moved
        if (player.getPlayerState() == PlayerState.AWAITING_DECISION) {
            if (canBuy(player.getPosition(), game)) {
                actions.add(PlayerActions.BUY_PROPERTY);
            }
            actions.add(PlayerActions.END_TURN);
        }
        return actions;
    }

    private static boolean canBuy(Integer playerPosition, GameDTO game) {
        List<Integer> allPositions = getAllOwnedPropertyPositions(game);
        return !allPositions.contains(playerPosition);
    }

    private static List<Integer> getAllOwnedPropertyPositions(GameDTO game) {
        List<Integer> positions = new ArrayList<>();
        game.getPlayers().forEach(p -> p.getOwnedProperties()
                .forEach(prop -> positions.add(prop.getBoardPosition())));
        return positions;
    }
}
