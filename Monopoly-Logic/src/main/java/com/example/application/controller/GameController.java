package com.example.application.controller;


import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.utility.GameMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {
    private final RedisTemplate<String, String> template;
    private final ObjectMapper mapper = new ObjectMapper();
    private final GameService gameService;

    @MutationMapping
    public GameDTO createNewGame() {
        Game game = new Game();
        game = gameService.save(game);
        GameDTO gameDTO = GameMapper.INSTANCE.GameToGameDTO(game);
//        var gameKey = "games:game:" + game.getGameId();
//        var value = mapper.writeValueAsString(gameDTO);
//        template.opsForValue().set(gameKey, value);
//        template.expire(gameKey, 120, TimeUnit.MINUTES);
        return gameDTO;
    }

    /*** TODO:
     * add exceptions and need to handle them,
     * also should add assertions and check for input data
     * player also should subscribe for this gameUpdate
     ***/
    @MutationMapping
    public GameDTO joinToGame(@Argument("gameId") UUID gameId,
                              @Argument("playerName") String playerName,
                              @Argument("playerColor") PlayerColors playerColor) {
        Game game = gameService.findById(gameId).orElseThrow();
        Player player = Player.builder()
                .name(playerName)
                .color(com.example.application.PlayerColors.valueOf(playerColor.toString()))
                .build();
        gameService.addPlayerToGame(player, game);
        var gameDto =gameService.findById(gameId)
                .map(GameMapper.INSTANCE::GameToGameDTO)
                .orElse(null);
        //template.convertAndSend("game-updates", gameDto);
        return gameDto;
    }

    @QueryMapping
    public List<GameDTO> getActiveGames() {
        var a = gameService.findAll().stream().map(GameMapper.INSTANCE::GameToGameDTO).collect(Collectors.toList());
        //template.convertAndSend("games:events:ACTIVE_GAMES_UPDATE",new ObjectMapper().writeValueAsString(a) );
        return a;
    }
    @QueryMapping
    public GameDTO findGameById(@Argument("id") String id) {
        Optional<Game> game = gameService.findById(UUID.fromString(id));
        assert game.isPresent();
        return GameMapper.INSTANCE.GameToGameDTO(game.get());
    }
}