package com.example.application.controller;

import com.example.application.components.DicePublisher;
import com.example.application.components.GamePublisher;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.DicePosition;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;
import java.util.concurrent.*;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GameSubscription {
    private final GamePublisher gamePublisher;
    private final DicePublisher dicePublisher;

    @SubscriptionMapping
    public Publisher<GameDTO> gameUpdated(@Argument("gameId") String gameId) {
        return gamePublisher.getPublisherForGame(gameId);
    }

    @SubscriptionMapping
    public Publisher<DicePosition> diceUpdated(@Argument("gameId") String gameId) {
        return dicePublisher.getPublisherForDice(gameId);
    }

}