package com.example.application.components;

import com.example.application.types.*;
import com.example.application.util.data.PropertyData;

import java.util.ArrayList;
import java.util.List;


/**
 * Resolves the available game and player actions based on the current game and player state.
 * <p>
 * This class provides static methods to determine which actions can be performed
 * by the game or a specific player at a given point in time.
 * </p>
 */
public class GameActionResolver {
    /**
     * Determines the list of available game-level actions based on the current state of the game.
     *
     * @param game the game data transfer object containing the current game state and players
     * @return a list of {@link GameActions} that are valid for the current game state
     */
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
    /**
     * Determines the list of available player-level actions based on the current state of the game and the player.
     *
     * @param game   the game data transfer object containing the current game state and players
     * @param player the player data transfer object for whom actions are being resolved
     * @return a list of {@link PlayerActions} that the player is allowed to perform at this point in the game
     */
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

        //player in jail
        if (player.getInJail_Turns() > 0) {
            if (player.getPlayerState() == PlayerState.AWAITING_DECISION) {
                actions.add(PlayerActions.END_TURN);
                return actions;
            }
            actions.add(PlayerActions.ROLL_DICE);
            return actions;
        }

        // Waiting for player response
        if (player.getPlayerState() == PlayerState.IDLE) {
            actions.add(PlayerActions.ROLL_DICE);
        }

        // player moved
        if (player.getPlayerState() == PlayerState.AWAITING_DECISION) {
            if (canBuy(player, game)) {
                actions.add(PlayerActions.BUY_PROPERTY);
            } else if (steppedOnUniqueTile(player.getPosition())) {
                actions.add(PlayerActions.SPECIAL_TILE_EFFECT);
                return actions;
            }
            actions.add(PlayerActions.END_TURN);
        }

        return actions;
    }

    private static boolean steppedOnUniqueTile(Integer playerPos) {
        var property = PropertyData.ofPos(playerPos);
        return property.cost() == 0;
    }

    private static boolean canBuy(PlayerDTO playerDTO, GameDTO game) {
        List<Integer> allPositions = getAllOwnedPropertyPositions(game);
        var playerPos = playerDTO.getPosition();
        var property = PropertyData.ofPos(playerPos);
        return !allPositions.contains(playerPos) && property.cost() != 0 && playerDTO.getBalance() > property.cost();
    }

    private static List<Integer> getAllOwnedPropertyPositions(GameDTO game) {
        List<Integer> positions = new ArrayList<>();
        game.getPlayers().forEach(p -> p.getOwnedProperties()
                .forEach(prop -> positions.add(prop.getBoardPosition())));
        return positions;
    }
}
