//package com.example.application.services.reactive;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.ReactiveSubscription;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Creates Completable-future with dice result.
// * Sends request via redis channel to Monopoly-Dice service.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DiceService_Mono {
//    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
//    private final ObjectMapper objectMapper;
//    private final Map<String, Mono<Integer>> activeRolls = new ConcurrentHashMap<>();
//
//    public Mono<Integer> rollDice(String gameId) {
//        String diceResultChannel = "game:diceResult";
//        String diceRollChannel = "game:+"+ gameId +":dice-roll-action";
//
//        if (activeRolls.containsKey(gameId)) {
//            return activeRolls.get(gameId);
//        }
//
//        Mono<Integer> diceMono = reactiveRedisTemplate.convertAndSend(diceRollChannel, "roll")
//                .doOnSuccess(count -> log.debug("Sent roll to {} subscribers", count))
//                .doOnError(e -> log.error("Failed to send roll command", e))
//                .thenMany(listenerContainer.receive(new ChannelTopic(diceResultChannel)))
//                .map(this::parseMessage)
//                .filter(msg -> msg != null && gameId.equals(msg.gameId()))
//                .map(DiceResult::diceValue)
//                .next()
//                .timeout(Duration.ofSeconds(10))
//                .doOnError(e -> log.warn("Dice roll failed or timed out: {}", e.getMessage()))
//                .doFinally(_ -> activeRolls.remove(gameId));
//
//        activeRolls.put(gameId, diceMono);
//        return diceMono;
//    }
//
//
//
//    private DiceResult parseMessage(ReactiveSubscription.Message<String, String> message) {
//        try {
//            String body = message.getMessage();
//            JsonNode node = objectMapper.readTree(body);
//            String gameId = node.get("gameId").asText();
//            int diceResult = node.get("diceResult").asInt();
//            return new DiceResult(gameId, diceResult);
//        } catch (Exception e) {
//            log.warn("Failed to parse dice result: {}", e.getMessage());
//            throw new IllegalArgumentException("Failed to parse dice result: " + e.getMessage());
//        }
//    }
//
//
//    private record DiceResult(String gameId, int diceValue) {}
//}