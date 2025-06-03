package com.example.application.services;


import com.example.application.entity.Player;
import com.example.application.repo.PlayerRepo;
import com.example.application.types.PlayerDTO;
import com.example.application.util.PropertyData;
import com.example.application.util.enums.PlayerState;
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
    public Optional<PlayerDTO> findById(UUID playerId) {
        return playerRepository.findById(playerId).map(GameMapper.INSTANCE::playerToDto);
    }

    @Transactional
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    @Transactional
    public void addPropertyToPlayer(UUID playerId, PropertyData propertyData) {
        Player player = playerRepository.findById(playerId).orElseThrow(EntityNotFoundException::new);
        player.addProperty(propertyData);
        player.setPlayerState(PlayerState.IDLE);
        player.setBalance(player.getBalance() - propertyData.cost());
        playerRepository.save(player);
    }
}