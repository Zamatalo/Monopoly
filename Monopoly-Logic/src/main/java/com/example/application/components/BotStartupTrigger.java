package com.example.application.components;

import com.example.application.services.BotService;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.types.GameState;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotStartupTrigger {

    private final GameService gameService;
    private final BotService botService;

    @PostConstruct
    public void triggerBotOnStartup() {
        gameService.findAll().stream()
                .filter(game -> game.getGameState() == GameState.IN_PROGRESS)
                .findFirst()
                .ifPresentOrElse(game -> {
                    log.info("Triggering bot for game ID: {}", game.getGameId());
                    botService.handleGameUpdate(game);
                }, () -> log.info("No IN_PROGRESS games found to trigger bot."));
    }
}
