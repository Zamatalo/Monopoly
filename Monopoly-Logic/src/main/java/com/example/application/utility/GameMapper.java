package com.example.application.utility;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = GameIdMapper.class)
public interface GameMapper {

    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    @Mapping(target = "gameActions", ignore = true)
    GameDTO GameToGameDTO(Game game);

    Game GameDTOtoGame(GameDTO gameDTO);

    @Mapping(target = "playerActions", ignore = true)
    PlayerDTO playerToDto(Player player);

    @Mapping(source = "gameId", target = "game")
    Player dtoToPlayer(PlayerDTO playerDTO);

}
@Component
class GameIdMapper {
    public Game map(UUID gameId) {
        if (gameId == null) {
            return null;
        }
        Game game = new Game();
        game.setGameId(gameId);
        return game;
    }
}