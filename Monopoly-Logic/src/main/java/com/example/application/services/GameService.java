package com.example.application.services;

import com.example.application.entity.Game;
import com.example.application.repo.GameRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {
    private final GameRepo gameRepo;

    public GameService(GameRepo gameRepo) {
        this.gameRepo = gameRepo;
    }

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
}
