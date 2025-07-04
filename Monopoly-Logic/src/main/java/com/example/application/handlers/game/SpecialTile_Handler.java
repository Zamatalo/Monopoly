package com.example.application.handlers.game;

import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.services.RedisService;
import com.example.application.services.TurnService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import config.GameConfig;
import data.SpecialTileData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpecialTile_Handler implements GameActionHandler {
    private final PlayerService playerService;
    private final TurnService turnService;
    private final RedisService redisService;
    private final GameService gameService;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String getAction() {
        return "SPECIAL_TILE";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = UUID.fromString(ctx.body().get("gameId"));
            var playerId = UUID.fromString(ctx.body().get("playerId"));
            GameDTO gameDTO = gameService.findById(gameId);
            PlayerDTO player = playerService.findById(playerId);

            SpecialTileData data = null;
            var position = player.getPosition();
            if (position.equals(2) || position.equals(17) || position.equals(33)) { //Chest
                data = SpecialTileData.CHEST_CARDS.get(random.nextInt(SpecialTileData.CHEST_CARDS.size()));

                switch (data.effect()) {
                    case MOVE_TO_GO_AND_COLLECT: {
                        player.setPosition(0);
                        player.setBalance(data.amount());
                        break;
                    }
                    case PAY: {
                        player.setBalance(player.getBalance() - data.amount());
                        break;
                    }
                    case COLLECT_FROM_EACH_PLAYER: {
                        player.setBalance(player.getBalance() + data.amount());
                        var amountForOne = data.amount() / gameDTO.getPlayers().size();
                        gameDTO.getPlayers().forEach(e -> e.setBalance(e.getBalance() - amountForOne));
                        break;
                    }
                    case GO_TO_JAIL: {
                        player.setPosition(10);
                        player.setInJail_Turns(4);
                        break;
                    }
                    case COLLECT: {
                        player.setBalance(player.getBalance() + data.amount());
                        break;
                    }
                }
            }

            if (position.equals(7) || position.equals(22) || position.equals(36)) { //Chance
                data = SpecialTileData.CHANCE_CARDS.get(random.nextInt(SpecialTileData.CHANCE_CARDS.size()));
                switch (data.effect()) {
                    case MOVE_TO_GO_AND_COLLECT: {
                        player.setPosition(0);
                        player.setBalance(data.amount());
                        break;
                    }
//                    case MOVE_TO_TILE_AND_COLLECT_IF_PASS_GO: {
//                        var randomTile = PropertyData.ALL.values().stream().filter(e -> e.cost() != 0).findAny();
//                        var currentPos = player.getPosition();
//                        assert randomTile.isPresent();
//
//                        boolean passedGo = randomTile.get().boardPosition() <= currentPos;
//                        if (passedGo) {
//                            player.setBalance(player.getBalance() + GameConfig.START_PAYOUT);
//                        }
//                        player.setPosition(randomTile.get().boardPosition());
//                        break;
//                    }
//                    case MOVE_TO_NEAREST_UTILITY: {
//
//                    }
//                    case MOVE_TO_NEAREST_RAILROAD: {
//
//                    }
                    case COLLECT: {
                        player.setBalance(player.getBalance() + data.amount());
                        break;
                    }
                    case GET_OUT_OF_JAIL_FREE: {
                        player.setInJail_Turns(0);
                        break;
                    }
//                    case MOVE_BACKWARD: {
//                        player.
//                    }
                    case GO_TO_JAIL: {
                        player.setPosition(10);
                        player.setInJail_Turns(4);
                        break;
                    }
                    case PAY: {
                        player.setBalance(player.getBalance() - data.amount());
                        break;
                    }
                    case PAY_EACH_PLAYER: {
                        var amountForOne = data.amount() / gameDTO.getPlayers().size() - 1;
                        gameDTO.getPlayers().forEach(e -> {

                            if (!e.getPlayerId().equals(player.getPlayerId())) {
                                e.setBalance(e.getBalance() + amountForOne); //other players
                            } else {
                                e.setBalance(e.getBalance() - amountForOne); //curr player
                            }
                        });
                        break;
                    }
                }
            }

            if (position.equals(30)) { //Go to jail
                data = SpecialTileData.JAIL;
                player.setPosition(10);
                player.setInJail_Turns(4);
            }

            if (position.equals(4)) {
                data = SpecialTileData.TAX;
                player.setBalance(player.getBalance() - data.amount());
            }

            if (position.equals(38)) {
                data = SpecialTileData.LUXURY_TAX;
                player.setBalance(player.getBalance() - data.amount());
            }

            if (position.equals(0)) {
                data = SpecialTileData.START;
                player.setBalance(player.getBalance());
            }


            List<PlayerDTO> updatedPlayers = gameDTO.getPlayers().stream()
                    .map(p -> p.getPlayerId().equals(player.getPlayerId()) ? player : p)
                    .toList();
            gameDTO.setPlayers(updatedPlayers);

            gameService.save(GameMapper.INSTANCE.GameDTOtoGame(gameDTO));
            turnService.endTurn(gameId);

            if (!isFromBot(ctx)) {
                assert data != null;
                log.info(data.toString());
                ctx.respond(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /// if its from bot, record should be ack
    public boolean isFromBot(RequestContextRedis ctx) {
        var a = ctx.body().get("isFromBot");
        return a != null && a.equals("true");
    }
}
