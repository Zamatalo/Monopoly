package com.example.application.services;

import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnService {
    private final GameService gameService;
    private final RedisService redisService;
    private final BotService botService;

    public void endTurn(UUID gameId) {
        GameDTO game = gameService.findById(gameId);
        PlayerDTO currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());


        int nextPlayerIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();
        game.setCurrentPlayerIndex(nextPlayerIndex);

        currentPlayer.setPlayerState(PlayerState.IDLE);

        List<PlayerDTO> updatedPlayers = game.getPlayers().stream()
                .map(p -> p.getPlayerId().equals(currentPlayer.getPlayerId()) ? currentPlayer : p)
                .toList();
        game.setPlayers(updatedPlayers);

        gameService.save(GameMapper.INSTANCE.GameDTOtoGame(game));
        redisService.publishTurnEnd(gameService.findById(gameId));
        /// check if the next player is bot
        var nextPlayer = game.getPlayers().get(nextPlayerIndex);
        if (Boolean.TRUE.equals(nextPlayer.getIsBot())) {
            botService.startBotTurn(gameId);
        }
    }

}