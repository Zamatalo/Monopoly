package com.example.application.services;


import com.example.application.PlayerColors;
import com.example.application.entity.Player;
import com.example.application.repo.PlayerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepo playerRepository;

    @Transactional
    public Player findOrCreatePlayer(UUID playerId, PlayerColors color) {
        return playerRepository.findById(playerId)
                .orElseGet(() -> playerRepository.save(
                        Player.builder().playerId(playerId).color(color).build()
                ));
    }
    @Transactional
    public Optional<Player> findPlayer(UUID playerId) {
        return playerRepository.findById(playerId);
    }
}