//package com.example.application.controller;
//
//import com.example.application.entity.Game;
//import com.example.application.services.GameService;
//import com.example.application.services.PlayerService;
//import com.example.application.util.enums.GameState;
//import com.example.application.util.enums.PlayerActions;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.stereotype.Controller;
//
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//public class GamePlayController {
//    private final GameService gameService;
//    private final PlayerService playerService;
//
//    @QueryMapping
//    public List<PlayerActions> getPossibleCurrentPlayerActions(@Argument("gameId") UUID gameId) {
//        Game game = gameService.findById(gameId)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//        if (!game.getGameState().equals(GameState.IN_PROGRESS)) {
//            return List.of(PlayerActions.START_GAME);
//        }
//
//        var playerId = game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId();
//        var player = playerService.findPlayer(playerId)
//                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
//
//        return List.of(PlayerActions.BUY_PROPERTY, PlayerActions.END_TURN, PlayerActions.ROLL_DICE);
//    }
//}
