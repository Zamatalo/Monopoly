package com.example.application.controller;

import com.example.application.services.PlayerService;
import com.example.application.types.PlayerDTO;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerController {
    private final PlayerService playerService;

    @QueryMapping
    public PlayerDTO getPlayer(@Argument("playerId") UUID playerId){
        var pl = playerService.findPlayer(playerId).orElseGet(null);
        return GameMapper.INSTANCE.playerToDto(pl);
    }
}
