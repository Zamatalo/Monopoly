package com.example.application.handlers;

import com.example.application.services.GameService;
import com.example.application.util.enums.GameActions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IsActionValid_Handler {
    private final GameService gameService;

    public boolean check(String action, String gameId) {
        try {
            GameActions requestedAction;
            try {
                requestedAction = GameActions.valueOf(action);
            } catch (IllegalArgumentException e) {
                return false;
            }

            List<GameActions> actions = gameService.resolveGameActions(UUID.fromString(gameId));
            return actions.contains(requestedAction);

        } catch (Exception e) {
            log.error("Exception occurred when checking if action {} is valid", action, e);
            return false;
        }
    }

}
