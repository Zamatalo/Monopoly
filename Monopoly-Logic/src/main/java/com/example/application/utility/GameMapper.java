package com.example.application.utility;

import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    GameDTO GameToGameDTO(Game game);
    Game GameDTOtoGame(GameDTO gameDTO);
    PlayerDTO playerToDto(Player player);
    Player dtoToPlayer(PlayerDTO playerDTO);
}

