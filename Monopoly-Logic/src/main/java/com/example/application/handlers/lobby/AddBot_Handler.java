package com.example.application.handlers.lobby;

import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.RedisService;
import com.example.application.types.PlayerColors;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RandomNameGenerator;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddBot_Handler implements GameActionHandler {
    private final GameService gameService;
    private final RedisService redisService;

    @Override
    public String getAction() {
        return "ADD_BOT";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = ctx.body().get("gameId");
            var game = gameService.findById(UUID.fromString(gameId));

            var allColors = PlayerColors.values();
            var usedColors = new ArrayList<PlayerColors>();

            game.getPlayers().forEach(p -> usedColors.add(p.getColor()));

            var color = Arrays.stream(allColors)
                    .filter(color1 -> !usedColors.contains(color1))
                    .findFirst();

            if (color.isEmpty()) {
                throw new IllegalArgumentException("Color not found");
            }

            Player bot = new Player();
            bot.setPlayerId(UUID.randomUUID());
            bot.setPlayerName(RandomNameGenerator.generateName());
            bot.setIsBot(true);
            bot.setColor(com.example.application.util.enums.PlayerColors.valueOf(color.get().toString()));

            var gameDTO = gameService.addPlayerToGame(bot, UUID.fromString(gameId));
            redisService.publishGameUpd(gameService.findById(UUID.fromString(gameId)));
            ctx.respond(gameDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
