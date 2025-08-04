package com.example.application.handlers.lobby;

import com.example.application.redis.RedisService_Mono;
import com.example.application.entity.Player;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.types.GameActions;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RandomNameGenerator;
import com.example.application.components.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddBot_Handler implements GameActionHandler {
    private final GameService_Mono gameService;
    private final RedisService_Mono redisService;

    @Override
    public String getAction() {
        return GameActions.ADD_BOT.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.getBody().get("gameId"));
            return gameService.findById_Mono(gameId)
                    .flatMap(game -> {
                        PlayerColors color = getFreeColor(game);
                        if (color == null) {
                            return ctx.respond("No available colors for bot");
                        }

                        Player bot = new Player();
                        bot.setPlayerId(UUID.randomUUID());
                        bot.setPlayerName(RandomNameGenerator.generateName());
                        bot.setIsBot(true);
                        bot.setColor(color);

                        return gameService.addPlayerToGame_Mono(bot, gameId)
                                .flatMap(gamUpd -> ctx.respond(gamUpd)
                                        .then(redisService.publishGameUpd(gamUpd))
                                );
                    })
                    .onErrorResume(e -> {
                        log.error("Error in handle(): {}", e.getMessage(), e);
                        return ctx.respond("Internal Server Error");
                    });
        } catch (IllegalArgumentException e) {
            return ctx.respond("Internal Server Error");
        }


    }

    private PlayerColors getFreeColor(GameDTO game) {
        var allColors = PlayerColors.values();
        var usedColors = new ArrayList<PlayerColors>();

        game.getPlayers().forEach(p -> usedColors.add(p.getColor()));

        return Arrays.stream(allColors)
                .filter(color1 -> !usedColors.contains(color1))
                .findFirst()
                .orElse(null);
    }

}
