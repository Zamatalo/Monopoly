package com.example.application.controller;

import com.example.application.service.GameGatewayService;
import com.example.application.types.*;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/// #TODO: add proper exceptions, and handle futures. Also make more controllers
@RequiredArgsConstructor
@Controller
public class GatewayController {
    private final GameGatewayService redisRequestReplyService;

    @MutationMapping
    public Mono<GameDTO> createNewGame() {
        return redisRequestReplyService.sendAction(
                GameActions.CREATE_GAME.name(),
                GameDTO.class);
    }

    @QueryMapping
    public Mono<List<GameDTO>> getAllGames() {
        return redisRequestReplyService.sendAction(
                GameActions.GET_ALL_GAMES.name(),
                GameDTO[].class
        ).map(Arrays::asList);
    }

    @QueryMapping
    public Mono<GameDTO> findGameByPlayerId(@Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                GameActions.FIND_GAME_PLAYER_ID.name(),
                Map.of("playerId", playerId.toString()),
                GameDTO.class);
    }

    @QueryMapping
    public Mono<GameDTO> findGameById(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                GameActions.FIND_GAME_BY_ID.name(),
                Map.of("gameId", gameId.toString()),
                GameDTO.class);
    }

    @MutationMapping
    public Mono<GameDTO> joinToGame(@Argument("gameId") UUID gameId,
                                    @Argument("playerName") String playerName,
                                    @Argument("playerColor") PlayerColors playerColor,
                                    @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                GameActions.JOIN_TO_GAME.name(),
                Map.of(
                        "gameId", gameId.toString(),
                        "playerName", playerName,
                        "playerColor", playerColor.toString(),
                        "playerId", playerId.toString()
                ),
                GameDTO.class);
    }

    @MutationMapping
    public Mono<GameDTO> addBotToGame(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                GameActions.ADD_BOT.name(),
                Map.of("gameId", gameId.toString()),
                GameDTO.class);
    }

    @QueryMapping
    public Mono<PlayerDTO> getPlayer(@Argument("playerId") UUID playerId,
                                     @Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                GameActions.GET_PLAYER.name(),
                Map.of(
                        "playerId", playerId.toString(),
                        "gameId", gameId.toString()
                ),
                PlayerDTO.class);
    }

    @MutationMapping
    public Mono<GameDTO> startGame(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAction(
                GameActions.START_GAME.name(),
                Map.of("gameId", gameId.toString()),
                GameDTO.class);
    }

    @MutationMapping
    public Mono<Integer> rollDice(@Argument("gameId") UUID gameId,
                                  @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                PlayerActions.ROLL_DICE.name(),
                Map.of(
                        "gameId", gameId.toString(),
                        "playerId", playerId.toString()
                ),
                Integer.class);
    }

    @MutationMapping
    public Mono<PropertyDTO> buyPropertyForPlayer(@Argument("gameId") UUID gameId,
                                                  @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                PlayerActions.BUY_PROPERTY.name(),
                Map.of(
                        "gameId", gameId.toString(),
                        "playerId", playerId.toString()
                ),
                PropertyDTO.class);
    }

    @MutationMapping
    public Mono<GameDTO> endTurn(@Argument("gameId") UUID gameId,
                                 @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                PlayerActions.END_TURN.name(),
                Map.of(
                        "gameId", gameId.toString(),
                        "playerId", playerId.toString()
                ),
                GameDTO.class);
    }

    @MutationMapping
    public Mono<SpecialTileData> resolveSpecialTile(@Argument("gameId") UUID gameId,
                                                    @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAction(
                PlayerActions.SPECIAL_TILE_EFFECT.name(),
                Map.of(
                        "gameId", gameId.toString(),
                        "playerId", playerId.toString()
                ),
                SpecialTileData.class);
    }
}
