package com.example.application.services;

import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnService {
    private final GameService gameService;
    private final RedisService redisService;

    public GameDTO endTurn(UUID gameId) {
        GameDTO game = gameService.findById(gameId);

        int nextPlayerIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();
        game.setCurrentPlayerIndex(nextPlayerIndex);
        var updGame= gameService.save(GameMapper.INSTANCE.GameDTOtoGame(game));
        redisService.publishTurnEnd(updGame);
        return updGame;
    }

}