package com.example.application.services;

import com.example.application.services.reactive.BotService_Mono;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.RedisService_Mono;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnService {
    private final GameService_Mono gameService;
    private final RedisService_Mono redisService;
    private final BotService_Mono botService;

    public void endTurn(UUID gameId) {
        GameDTO game = gameService.findById_Mono(gameId);
        PlayerDTO currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());

        //If in jail
        if (currentPlayer.getInJail_Turns() > 0) {
            currentPlayer.setInJail_Turns(currentPlayer.getInJail_Turns() - 1);
        }

        int nextPlayerIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();
        game.setCurrentPlayerIndex(nextPlayerIndex);

        currentPlayer.setPlayerState(PlayerState.IDLE);

        List<PlayerDTO> updatedPlayers = game.getPlayers().stream()
                .map(p -> p.getPlayerId().equals(currentPlayer.getPlayerId()) ? currentPlayer : p)
                .toList();
        game.setPlayers(updatedPlayers);


        gameService.save_Mono(GameMapper.INSTANCE.GameDTOtoGame(game));
        redisService.publishTurnEnd(gameService.findById_Mono(gameId));

        /// check if the next player is bot
        var nextPlayer = game.getPlayers().get(nextPlayerIndex);
        if (Boolean.TRUE.equals(nextPlayer.getIsBot())) {
            Mono.fromCallable(()-> botService.startBotTurn(gameId));
        }
    }

}