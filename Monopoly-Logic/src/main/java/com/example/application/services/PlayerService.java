package com.example.application.services;


import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.repo.PlayerRepo;
import com.example.application.util.PropertyData;
import com.example.application.util.enums.GameState;
import com.example.application.util.enums.PlayerActions;
import com.example.application.util.enums.PlayerState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepo playerRepository;

    @Transactional(readOnly = true)
    public Optional<Player> findById(UUID playerId) {
        return playerRepository.findById(playerId);
    }

    @Transactional
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    @Transactional(readOnly = true)
    public List<PlayerActions> resolvePlayerActions(UUID playerId) {
        var player = playerRepository.findById(playerId).orElseThrow();
        var game = player.getGame();
        var actions = new ArrayList<PlayerActions>();

        if (!game.getGameState().equals(GameState.IN_PROGRESS)) {
            return actions;
        }
        if (!game.getCurrentPlayer().getPlayerId().equals(playerId)) {
            return actions;
        }

        if (player.getPlayerState() == PlayerState.IDLE) {
            actions.add(PlayerActions.ROLL_DICE);
        }

        if (player.getPlayerState() == PlayerState.MOVED) {
            if (canBuy(player.getPosition(), game)) {
                actions.add(PlayerActions.BUY_PROPERTY);
            }
            actions.add(PlayerActions.END_TURN);
        }
        return actions;
    }

    private boolean canBuy(Integer playerPosition, Game game) {
        return game.getPlayers().stream()
                .flatMap(p -> p.getOwnedProperties().stream())
                .map(PropertyData::boardPosition)
                .noneMatch(pos -> pos.equals(playerPosition));
    }

}