//package com.example.application.redis;
//
//import com.example.application.services.ActionService;
//import com.example.application.util.enums.GameActions;
//import com.example.application.util.enums.PlayerActions;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.pubsub.RedisPubSubAdapter;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class Redis_ActionsRequestListener {
//    private final RedisClient redisClient;
//    private final ObjectMapper objectMapper;
//    private final ActionService actionService;
//
//            @Override
//            public void message(String channel, String message) {
//                try {
//                    Map<String, Object> payload = objectMapper.readValue(message, Map.class);
//                    String requestId = (String) payload.get("requestId");
//                    String responseChannel = (String) payload.get("responseChannel");
//                    Map<String, Object> body = (Map<String, Object>) payload.get("body");
//
//                    switch (channel) {
//                        case "game.getActions":
//                            handleGameActions(requestId, responseChannel, body);
//                            break;
//                        case "game.player.getActions":
//                            handlePlayerActions(requestId, responseChannel, body);
//                            break;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    private void handleGameActions(String requestId, String responseChannel, Map<String, Object> body) {
//        UUID gameId = UUID.fromString((String) body.get("gameId"));
//
//        List<GameActions> actions = actionService.resolveGameActions(gameId);
//        sendResponse(responseChannel, actions);
//    }
//
//    private void handlePlayerActions(String requestId, String responseChannel, Map<String, Object> body) {
//        UUID playerId = UUID.fromString((String) body.get("playerId"));
//
//        List<PlayerActions> actions = actionService.resolvePlayerActions(playerId);
//        sendResponse(responseChannel, actions);
//    }
//
//    private void sendResponse(String channel, Object response) {
//        try (var conn = redisClient.connect()) {
//            var async = conn.async();
//            String payload = objectMapper.writeValueAsString(response);
//            async.publish(channel, payload);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}