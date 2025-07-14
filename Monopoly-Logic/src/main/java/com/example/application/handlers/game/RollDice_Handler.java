package com.example.application.handlers.game;

import com.example.application.services.imperative.PlayerService;
import com.example.application.services.reactive.BotService_Mono;
import com.example.application.services.reactive.DiceService_Mono;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.RedisService_Mono;
import config.GameConfig;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RollDice_Handler implements GameActionHandler {
    private final DiceService_Mono diceService;
    private final GameService_Mono gameService;
    private final PlayerService playerService;
    private final RedisService_Mono redisService;
    private final BotService_Mono botService;

    @Override
    public String getAction() {
        return "ROLL_DICE";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        var gameId = UUID.fromString(ctx.body().get("gameId"));
        var playerId = UUID.fromString(ctx.body().get("playerId"));
        CompletableFuture<Integer> future = diceService.rollDice(gameId.toString());
        var rolledResult = future.join();
        GameDTO game = gameService.findById_Mono(gameId);
        PlayerDTO player = playerService.findById(playerId);

        if (player.getInJail_Turns()>0){
            rolledResult= 0;
        }
        int newPosition = (player.getPosition() + rolledResult) % 40;


        PropertyData steppedOn = steppedOnAnotherPlayerField(gameId, newPosition);
        if (steppedOn != null) {
            player.setBalance(player.getBalance() - steppedOn.cost());
            PlayerDTO player_toPayTo = playerService.getPlayer_forProperty_forGame(gameId, steppedOn);
            PlayerDTO palyer_tpPayTo_inGame = game
                    .getPlayers().stream()
                    .filter(e->e.getPlayerId().equals(player_toPayTo.getPlayerId()))
                    .findFirst()
                    .orElse(null);
            assert palyer_tpPayTo_inGame != null;
            palyer_tpPayTo_inGame.setBalance(player_toPayTo.getBalance() + steppedOn.cost());
        }

        if (passedGo(player.getPosition(), newPosition)) {
            player.setBalance(player.getBalance() + GameConfig.START_PAYOUT);
        }

        player.setPlayerState(PlayerState.AWAITING_DECISION);
        player.setPosition(newPosition);
        //player.setBalance(steppedOnSpecialTile(player));

        List<PlayerDTO> updatedPlayers = game.getPlayers().stream()
                .map(p -> p.getPlayerId().equals(player.getPlayerId()) ? player : p)
                .toList();
        game.setPlayers(updatedPlayers);

        gameService.save_Mono(GameMapper.INSTANCE.GameDTOtoGame(game));
        redisService.publishGameUpd(gameService.findById_Mono(gameId));

        if (!isFromBot(ctx)) {
            ctx.respond(rolledResult);
        }else{
            GameDTO updatedGame = gameService.findById_Mono(gameId);
            botService.handelAfterRollAction(updatedGame);
        }
    }

    private boolean passedGo(Integer prevPost, Integer nextPos) {
        return nextPos < prevPost;
    }


    /// not for every Special tile, only for easy ones.
    /// the ones with complex logic should have dedicated Handler
//    private Integer steppedOnSpecialTile(PlayerDTO player) {
//        var property = PropertyData.ofPos(player.getPosition());
//        if (property.cost() == 0) {
//            if (property.boardPosition() == 0) { //Start Tile
//                player.setBalance(player.getBalance() + GameConfig.START_PAYOUT);
//            }
////            if (property.boardPosition() == 4) { //Income Tax
////                player.setBalance(player.getBalance() - GameConfig.INCOME_TAX);
////            }
//        }
//        return player.getBalance();
//    }

    private PropertyData steppedOnAnotherPlayerField(UUID gameId, Integer playerPos) {
        var properties = gameService.findAllProperties_Mono(gameId);
        return properties.stream()
                .filter(propertyData -> propertyData.boardPosition() == playerPos)
                .findFirst()
                .orElse(null);
    }

    /// if its from bot, record should be ack
    public boolean isFromBot(RequestContextRedis ctx) {
        var a = ctx.body().get("sentFromBot");
        return a != null && a.equals("true");
    }
}
