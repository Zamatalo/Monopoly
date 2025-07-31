package com.example.application.utility;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GameMapper {

    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    @Mapping(target = "gameActions", ignore = true)
    GameDTO GameToGameDTO(Game game);

    @Mapping(target = "createdTime", ignore = true)
    Game GameDTOtoGame(GameDTO gameDTO);

    @Mapping(target = "playerActions", ignore = true)
    PlayerDTO playerToDto(Player player);

    @Mapping(target = "game", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    Player dtoToPlayer(PlayerDTO playerDTO);

    @AfterMapping
    default void linkPlayers(@MappingTarget Game game) {
        if (game.getPlayers() != null) {
            for (Player player : game.getPlayers()) {
                player.setGame(game);
            }
        }
    }
}
