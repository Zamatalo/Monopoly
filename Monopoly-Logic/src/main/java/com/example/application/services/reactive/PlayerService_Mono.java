package com.example.application.services.reactive;


import com.example.application.entity.Player;
import com.example.application.repo.PlayerRepo;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService_Mono {
    private final PlayerRepo playerRepository;

    @Transactional(readOnly = true)
    public Mono<PlayerDTO> findById(UUID playerId) {
        return Mono.fromCallable(() -> {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(EntityNotFoundException::new);
            return GameMapper.INSTANCE.playerToDto(player);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<Boolean> existsById(UUID playerId) {
        return Mono.fromCallable(() ->
                        playerRepository.existsById(playerId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<PlayerDTO> addPropertyToPlayer(UUID playerId, PropertyData propertyData) {
        return Mono.fromCallable(() -> {
            Player player = playerRepository
                    .findById(playerId)
                    .orElseThrow(EntityNotFoundException::new);
            PropertyData property = new PropertyData(propertyData.displayName(),propertyData.boardPosition(),propertyData.cost(),propertyData.upgradable(),player.getPlayerId());

            player.addProperty(property);
            player.setPlayerState(PlayerState.IDLE);
            player.setBalance(player.getBalance() - propertyData.cost());

            return GameMapper.INSTANCE.playerToDto(
                    playerRepository.save(player)
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<PlayerDTO> getPlayer_forProperty_forGame(UUID gameId, PropertyData property) {
        return Mono.fromCallable(() -> {
            Player player = playerRepository.findPlayerByGameIdAndProperty(gameId, property)
                    .orElseThrow(EntityNotFoundException::new);
            return GameMapper.INSTANCE.playerToDto(player);
        }).subscribeOn(Schedulers.boundedElastic());
    }

}