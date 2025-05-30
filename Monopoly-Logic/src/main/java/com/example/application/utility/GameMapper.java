package com.example.application.utility;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);
    @Mapping(target = "gameActions", ignore = true)
    GameDTO GameToGameDTO(Game game);

    Game GameDTOtoGame(GameDTO gameDTO);

    @Mapping(target = "playerActions", ignore = true)
    PlayerDTO playerToDto(Player player);

    Player dtoToPlayer(PlayerDTO playerDTO);
}

