//package com.example.application.services.imperative;
//
//import com.example.application.components.GameActionResolver;
//import com.example.application.entity.Game;
//import com.example.application.entity.Player;
//import com.example.application.repo.GameRepo;
//import com.example.application.types.GameDTO;
//import com.example.application.util.data.PropertyData;
//import com.example.application.util.enums.GameState;
//import com.example.application.utility.GameMapper;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Service
//public class GameService {
//    private final GameRepo gameRepo;
//
//    @Transactional(readOnly = true)
//    public List<GameDTO> findAll() {
//        return gameRepo.findAll()
//                .stream()
//                .map(this::toEnrichedGameDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public GameDTO findById(UUID id) {
//        var game = gameRepo.findById(id)
//                .orElseThrow(EntityNotFoundException::new);
//        return toEnrichedGameDTO(game);
//    }
//
//    @Transactional
//    public GameDTO save(Game game) {
//        Game savedGame = gameRepo.save(game);
//        return toEnrichedGameDTO(savedGame);
//    }
//
//    @Transactional
//    public GameDTO addPlayerToGame(Player player, UUID gameId) {
//        Game game = gameRepo.findById(gameId).orElseThrow();
//        game.addPlayer(player);
//        return save(game);
//    }
//
//    @Transactional
//    public GameDTO startGame(UUID gameId) {
//        Game game = gameRepo.findById(gameId).orElseThrow();
//
//        if (game.getGameState() != GameState.STARTED || game.getPlayers().size() != 4) {
//            throw new IllegalStateException("Game cannot be started");
//        }
//
//        game.setGameState(GameState.IN_PROGRESS);
//        Game savedGame = gameRepo.save(game);
//        return toEnrichedGameDTO(savedGame);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<GameDTO> findGameByPlayerId(UUID playerId) {
//        return gameRepo.findGameByPlayerId(playerId).map(this::toEnrichedGameDTO);
//    }
//
//    @Transactional(readOnly = true)
//    public List<PropertyData> findAllProperties(UUID gameId) {
//        Game game = gameRepo.findById(gameId)
//                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + gameId));
//
//        List<PropertyData> allProperties = new ArrayList<>();
//
//        for (Player player : game.getPlayers()) {
//            allProperties.addAll(player.getOwnedProperties());
//        }
//
//        return allProperties;
//    }
//
//    private GameDTO toEnrichedGameDTO(Game game) {
//        GameDTO dto = GameMapper.INSTANCE.GameToGameDTO(game);
//        dto.setGameActions(GameActionResolver.resolveGameActions(dto));
//
//        dto.getPlayers().forEach(playerDTO -> {
//            var actions = GameActionResolver.resolvePlayerActions(dto, playerDTO);
//            playerDTO.setPlayerActions(actions);
//        });
//
//        return dto;
//    }
//
//}