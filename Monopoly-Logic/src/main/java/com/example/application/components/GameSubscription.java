package com.example.application.components;

import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.FluxSink;

import java.util.Random;

@Component
@Slf4j
public class GameSubscription {
    private final GameService gameService;
    private FluxSink<GameDTO> gamesSink;
    private Random random = new Random();
    private ConnectableFlux<GameDTO> gamesPublisher;
    public GameSubscription(GameService gameService) {
        this.gameService = gameService;
    }




}