//package com.example.application.configuration;
//
//import com.example.application.services.ActionService;
//import com.example.application.services.GameService;
//import com.example.application.util.enums.GameActions;
//import com.example.application.util.enums.PlayerActions;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RedisReceiver implements MessageListener {
//    private final ActionService actionService;
//    private final ObjectMapper objectMapper;
//    private final StringRedisTemplate redisTemplate;
//    private final GameService gameService;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        try {
//            String channel = new String(message.getChannel());
//            String messageBody = new String(message.getBody());
//
//            Map<String, Object> payload = parseMessage(messageBody);
//            String requestId = (String) payload.get("correlationId");
//            String responseChannel = (String) payload.get("replyChannel");
//            Map<String, Object> body = (Map<String, Object>) payload.get("payload");
//
//            var channelList = Arrays.stream(channel.split(":")).toList();
//
//
//            if (channelList.containsAll(List.of("game", "getActions"))) {
//                handleGameActions(requestId, responseChannel, body);
//            } else if (channelList.containsAll(List.of("game", "player", "getActions"))) {
//                handlePlayerActions(requestId, responseChannel, body);
//            } else if (channelList.containsAll(List.of("game", "player", "action"))) {
//                handeMakePlayerAction(requestId, responseChannel, body);
//            } else if (channelList.containsAll(List.of("game", "action"))) {
//                handeMakeGameAction(requestId, responseChannel, body);
//            } else if (channelList.contains("getAllGames")) {
//                handeGetAllGames(requestId, responseChannel, body);
//            } else {
//                log.warn("Unknown channel: {}", channel);
//            }
//        } catch (Exception e) {
//            log.error("Error processing Redis message", e);
//        }
//    }
//
//    private Map<String, Object> parseMessage(String message) throws JsonProcessingException {
//        return objectMapper.readValue(message, Map.class);
//    }
//
//    private void handleGameActions(String requestId, String responseChannel, Map<String, Object> body) {
//        UUID gameId = UUID.fromString((String) body.get("gameId"));
//        List<GameActions> actions = actionService.resolveGameActions(gameId);
//        sendResponse(responseChannel, Map.of(
//                "correlationId", requestId,
//                "payload", actions
//        ));
//    }
//
//    private void handlePlayerActions(String requestId, String responseChannel, Map<String, Object> body) {
//        UUID playerId = UUID.fromString((String) body.get("playerId"));
//        List<PlayerActions> actions = actionService.resolvePlayerActions(playerId);
//        sendResponse(responseChannel, Map.of(
//                "correlationId", requestId,
//                "payload", actions
//        ));
//    }
//
//    private void handeMakePlayerAction(String requestId, String responseChannel, Map<String, Object> body) {
//        var action = (PlayerActions) body.get("action");
//        var gameId = UUID.fromString((String) body.get("gameId"));
//
//        actionService.makePlayerAction(gameId, action);
//        //closing the response
//        sendResponse(responseChannel, Map.of("correlationId", requestId));
//    }
//
//    private void handeMakeGameAction(String requestId, String responseChannel, Map<String, Object> body) {
//        var action = (GameActions) body.get("action");
//        var gameId = UUID.fromString((String) body.get("gameId"));
//
//        actionService.makeGameAction(gameId, action);
//        sendResponse(responseChannel, Map.of("correlationId", requestId));
//    }
//
//    private void handeGetAllGames(String requestId, String responseChannel, Map<String, Object> body) {
//        var allgames = gameService.findAll();
//        sendResponse(responseChannel,
//                Map.of("correlationId", requestId,
//                "payload", allgames)
//        );
//    }
//
//    private void sendResponse(String channel, Object response) {
//        try {
//            String responseBody = objectMapper.writeValueAsString(response);
//            redisTemplate.convertAndSend(channel, responseBody);
//        } catch (JsonProcessingException e) {
//            log.error("Failed to serialize response", e);
//        }
//    }
//}