package com.example.application.handlers.game;

import com.example.application.data.SpecialTileData;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.services.reactive.TurnService_Mono;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpecialTile_Handler implements GameActionHandler {
    private final PlayerService_Mono playerService;
    private final TurnService_Mono turnService;
    private final GameService_Mono gameService;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String getAction() {
        return PlayerActions.SPECIAL_TILE_EFFECT.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            var gameId = UUID.fromString(ctx.body().get("gameId"));
            var playerId = UUID.fromString(ctx.body().get("playerId"));

            return Mono.zip(
                    gameService.findById_Mono(gameId),
                    playerService.findById(playerId)
            ).flatMap(tuple -> {
                GameDTO gameDTO = tuple.getT1();
                PlayerDTO player = tuple.getT2();

                int position = player.getPosition();
                SpecialTileData data = SpecialTileData.NONE;

                // CHEST
                if (position == 2 || position == 17 || position == 33) {
                    data = SpecialTileData.CHEST_CARDS.get(random.nextInt(SpecialTileData.CHEST_CARDS.size()));
                    applyEffect(data, player, gameDTO);
                }

                // CHANCE
                else if (position == 7 || position == 22 || position == 36) {
                    data = SpecialTileData.CHANCE_CARDS.get(random.nextInt(SpecialTileData.CHANCE_CARDS.size()));
                    applyEffect(data, player, gameDTO);
                }

                // GO TO JAIL TILE
                else if (position == 30) {
                    data = SpecialTileData.JAIL;
                    player.setPosition(10);
                    player.setInJail_Turns(4);
                }
                // TAX TILE
                else if (position == 4) {
                    data = SpecialTileData.TAX;
                    player.setBalance(player.getBalance() - data.amount());
                }

                // LUXURY TAX TILE
                else if (position == 38) {
                    data = SpecialTileData.LUXURY_TAX;
                    player.setBalance(player.getBalance() - data.amount());
                }

                // START TILE ()
                else if (position == 0) {
                    data = SpecialTileData.START;
                }

                // Update game state
                List<PlayerDTO> updatedPlayers = gameDTO.getPlayers().stream()
                        .map(p -> p.getPlayerId().equals(player.getPlayerId()) ? player : p)
                        .toList();
                gameDTO.setPlayers(updatedPlayers);

                return gameService.save_Mono(GameMapper.INSTANCE.GameDTOtoGame(gameDTO))
                        .thenReturn(data);

            }).flatMap(data -> {
                if (!isFromBot(ctx)) {
                    log.info("Special tile effect: {}", data);
                    return ctx.respond(data)
                            .then(turnService.endTurn(gameId));
                } else {
                    return turnService.endTurn(gameId); // ack for botww
                }
            }).onErrorResume(e -> {
                log.error("Error in SpecialTile_Handler: {}", e.getMessage(), e);
                return ctx.respond("Internal server error.");
            });

        } catch (Exception e) {
            log.error("Parsing error in SpecialTile_Handler", e);
            return ctx.respond("Invalid input format.");
        }
    }

    private void applyEffect(SpecialTileData data, PlayerDTO player, GameDTO gameDTO) {
        switch (data.effect()) {
            case MOVE_TO_GO_AND_COLLECT -> {
                player.setPosition(0);
                player.setBalance(player.getBalance() + data.amount());
            }
            case PAY -> player.setBalance(player.getBalance() - data.amount());

            case COLLECT_FROM_EACH_PLAYER -> {
                int total = data.amount();
                int perPlayer = total / gameDTO.getPlayers().size();
                player.setBalance(player.getBalance() + total);
                gameDTO.getPlayers().forEach(p -> p.setBalance(p.getBalance() - perPlayer));
            }
            case GO_TO_JAIL -> {
                player.setPosition(10);
                player.setInJail_Turns(4);
            }
            case COLLECT -> player.setBalance(player.getBalance() + data.amount());

            case GET_OUT_OF_JAIL_FREE -> player.setInJail_Turns(0);

            case PAY_EACH_PLAYER -> {
                int total = data.amount();
                int perPlayer = total / (gameDTO.getPlayers().size() - 1);

                gameDTO.getPlayers().forEach(p -> {
                    if (!p.getPlayerId().equals(player.getPlayerId())) {
                        p.setBalance(p.getBalance() + perPlayer);
                    } else {
                        p.setBalance(p.getBalance() - total);
                    }
                });
            }
        }
    }

    public boolean isFromBot(RequestContextRedis ctx) {
        return "true".equals(ctx.body().get("sentFromBot"));
    }
}
