package com.example.application.services;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.repo.GameRepo;
import com.example.application.util.PropertyData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GameService {
    private final GameRepo gameRepo;

    @Transactional(readOnly = true)
    public List<Game> findAll() {
        return gameRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Game> findById(UUID id) {
        return gameRepo.findById(id);
    }

    @Transactional
    public Game save(Game game) {
        return gameRepo.save(game);
    }

    @Transactional
    public void deleteById(UUID id) {
        gameRepo.deleteById(id);
    }

    @Transactional
    public void addPlayerToGame(Player player, Game game) {
        game.getPlayers().add(player);
        var gm = gameRepo.save(game);
       // return gm.getPlayers().stream().filter(e -> e.getPlayerId().equals(player.getPlayerId())).findFirst().orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<Game> findGameByPlayerId (UUID playerId) {
        return gameRepo.findGameByPlayerId(playerId);
    }

    @Transactional(readOnly = true)
    public List<PropertyData> findProperties_AllPlayers_ForGame(UUID gameId) {
        var gm = gameRepo.findById(gameId);
        List<PropertyData> list = new ArrayList<>();

        gm.get().getPlayers().forEach(player -> {
            list.addAll(player.getOwnedProperties());
        });
        return list;
    }
}
