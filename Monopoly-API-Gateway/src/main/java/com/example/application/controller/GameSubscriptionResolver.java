//package com.example.application.controller;
//
//import com.example.application.components.publishers.DicePublisher;
//import com.example.application.components.publishers.GamePublisher;
//import com.example.application.types.DicePosition;
//import com.example.application.types.GameDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.reactivestreams.Publisher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
//import org.springframework.stereotype.Controller;
//
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//@Slf4j
//@Controller
//public class GameSubscriptionResolver {
//    private final GamePublisher gamePublisher;
//    private final DicePublisher dicePublisher;
//
//    @SubscriptionMapping
//    public Publisher<GameDTO> gameUpdated(@Argument("gameId") String gameId) {
//        return gamePublisher.getPublisherForGame(gameId);
//    }
//
//    @SubscriptionMapping
//    public Publisher<DicePosition> diceUpdated(@Argument("gameId") String gameId) {
//        return dicePublisher.getPublisherForDice(gameId);
//    }
//}
