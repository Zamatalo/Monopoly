package com.example.application.handlers.lobby;

import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.util.enums.GameState;
import com.example.application.util.enums.PlayerColors;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddBot_Handler implements GameActionHandler {
    private final GameService gameService;

    @Override
    public String getAction() {
        return "ADD_BOT";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = ctx.body().get("gameId");

            var game = gameService.findById(UUID.fromString(gameId)).get();

            if ((game.getPlayers().size() >= 4) || !game.getGameState().equals(GameState.STARTED)) {
                throw new IllegalArgumentException("Already 4 players");
            }

            var allColors = PlayerColors.values();
            var usedColors = new ArrayList<>();

            game.getPlayers().forEach(p -> usedColors.add(p.getColor()));

            var color = Arrays.stream(allColors)
                    .filter(color1 -> !usedColors.contains(color1))
                    .findFirst();

            if (color.isEmpty()) {
                throw new IllegalArgumentException("Color not found");
            }

            Player bot = new Player();
            bot.setPlayerId(UUID.randomUUID());
            bot.setPlayerName("Bot " + LocalTime.now().getSecond());
            bot.setBot(true);
            bot.setColor(color.get());

            var gameDTO =gameService.addPlayerToGame(bot, UUID.fromString(gameId));
            ctx.respond(gameDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
