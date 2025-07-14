package com.example.application.services.imperative;


import com.example.application.entity.Player;
import com.example.application.repo.PlayerRepo;
import com.example.application.types.PlayerDTO;
import com.example.application.util.data.PropertyData;
import com.example.application.util.enums.PlayerState;
import com.example.application.utility.GameMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepo playerRepository;

    @Transactional(readOnly = true)
    public PlayerDTO findById(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        return GameMapper.INSTANCE.playerToDto(player);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID playerId) {
        return playerRepository.existsById(playerId);
    }

    @Transactional
    public void addPropertyToPlayer(UUID playerId, PropertyData propertyData) {
        Player player = playerRepository.findById(playerId).orElseThrow(EntityNotFoundException::new);
        player.addProperty(propertyData);
        player.setPlayerState(PlayerState.IDLE);
        player.setBalance(player.getBalance() - propertyData.cost());
        playerRepository.save(player);
    }

    @Transactional(readOnly = true)
    public PlayerDTO getPlayer_forProperty_forGame(UUID gameId, PropertyData property) {
        Player player = playerRepository.findPlayerByGameIdAndProperty(gameId, property)
                .orElseThrow(() -> new EntityNotFoundException("Player for property not found"));
        return GameMapper.INSTANCE.playerToDto(player);
    }
}