package com.example.application.utility;

import com.example.application.dto.GameDTO;
import com.example.application.dto.PlayerDTO;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    GameDTO GameToGameDTO(Game game);

    Game GameDTOtoGame(GameDTO gameDTO);

    PlayerDTO playerToDto(Player player);

    Player dtoToPlayer(PlayerDTO playerDTO);
}



