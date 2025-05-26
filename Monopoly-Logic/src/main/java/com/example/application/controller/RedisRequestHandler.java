//package com.example.application.controller;
//
//import com.example.application.services.GameService;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.pubsub.RedisPubSubAdapter;
//import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class RedisRequestHandler {
//
//    private final RedisClient redisClient;
//    private final ObjectMapper objectMapper;
//    private final GameService gameLogicService;
//
//    @PostConstruct
//    public void subscribeToRedisChannels() {
//        StatefulRedisPubSubConnection<String, String> conn = redisClient.connectPubSub();
//        conn.addListener(new RedisPubSubAdapter<>() {
//            @Override
//            public void message(String channel, String message) {
//                handleMessage(channel, message);
//            }
//        });
//
//        conn.async().subscribe("game.getActions", "game.player.getActions");
//    }
//
//    private void handleMessage(String channel, String rawJson) {
//        try {
//            JsonNode root = objectMapper.readTree(rawJson);
//            String requestId = root.get("requestId").asText();
//            String responseChannel = root.get("responseChannel").asText();
//            JsonNode body = root.get("body");
//
//            Object responseObject;
//
//            if ("game.getActions".equals(channel)) {
//                UUID gameId = UUID.fromString(body.get("gameId").asText());
//                responseObject = gameLogicService.getGameActions(gameId);
//
//            } else if ("game.player.getActions".equals(channel)) {
//                UUID gameId = UUID.fromString(body.get("gameId").asText());
//                UUID playerId = UUID.fromString(body.get("playerId").asText());
//                responseObject = gameLogicService.getPlayerActions(gameId, playerId);
//
//            } else {
//                throw new IllegalStateException("Unknown channel: " + channel);
//            }
//
//            String response = objectMapper.writeValueAsString(responseObject);
//            redisClient.connect().async().publish(responseChannel, response);
//
//        } catch (Exception e) {
//            e.printStackTrace(); // Лучше логгировать
//        }
//    }
//}
