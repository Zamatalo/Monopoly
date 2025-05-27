package com.example.application.services;


import com.example.application.entity.Player;
import com.example.application.repo.PlayerRepo;
import com.example.application.types.PlayerDTO;
import com.example.application.utility.GameMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepo playerRepository;

    @Transactional(readOnly = true)
    public PlayerDTO findById(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(()-> new EntityNotFoundException("Player with id: " + playerId + " not found"));
        return GameMapper.INSTANCE.playerToDto(player);
    }

    @Transactional
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

}