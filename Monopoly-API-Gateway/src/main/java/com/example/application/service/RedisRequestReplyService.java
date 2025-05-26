//package com.example.application.service;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.api.StatefulRedisConnection;
//import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
//import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.*;
//
//@Service
//@RequiredArgsConstructor
//public class RedisRequestReplyService {
//
//    private final RedisClient redisClient;
//    private final ObjectMapper objectMapper;
//
//    private StatefulRedisConnection<String, String> redisConnection;
//    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
//    private RedisPubSubReactiveCommands<String, String> pubSub;
//
//    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
//    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
//
//    @PostConstruct
//    public void init() {
//        redisConnection = redisClient.connect();
//        pubSubConnection = redisClient.connectPubSub();
//        pubSub = pubSubConnection.reactive();
//
//        pubSub.subscribe("gateway:replies").subscribe();
//
//        pubSub.observeChannels()
//                .doOnNext(message -> {
//                    String raw = message.getMessage();
//                    try {
//                        Map<String, Object> reply = objectMapper.readValue(raw, new TypeReference<>() {});
//                        String correlationId = (String) reply.get("correlationId");
//                        String payload = objectMapper.writeValueAsString(reply.get("payload"));
//
//                        CompletableFuture<String> future = pendingRequests.remove(correlationId);
//                        if (future != null) {
//                            future.complete(payload);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                })
//                .subscribe();
//    }
//
//    public <T> CompletableFuture<List<T>> sendAndReceive(
//            String topic,
//            Map<String, Object> request,
//            Class<T> itemType) {
//
//        String correlationId = UUID.randomUUID().toString();
//        String replyChannel = "gateway:replies";
//
//        CompletableFuture<String> future = new CompletableFuture<>();
//        pendingRequests.put(correlationId, future);
//
//        timeoutExecutor.schedule(() -> {
//            if (!future.isDone()) {
//                future.completeExceptionally(
//                        new TimeoutException("Timeout waiting for response for " + correlationId)
//                );
//                pendingRequests.remove(correlationId);
//            }
//        }, 5, TimeUnit.SECONDS);
//
//        Map<String, Object> payload = Map.of(
//                "correlationId", correlationId,
//                "replyChannel", replyChannel,
//                "payload", request
//        );
//
//        try {
//            String json = objectMapper.writeValueAsString(payload);
//            redisConnection.sync().publish(topic, json);
//        } catch (Exception e) {
//            future.completeExceptionally(e);
//        }
//
//        return future.thenApply(json -> {
//            try {
//                return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, itemType));
//            } catch (Exception e) {
//                throw new CompletionException(e);
//            }
//        });
//    }
//
//    public <T> CompletableFuture<List<T>> sendAndReceive(String topic, Class<T> returnType) {
//        return sendAndReceive(topic, Map.of(), returnType);
//    }
//
//    @PreDestroy
//    public void cleanup() {
//        redisConnection.close();
//        pubSubConnection.close();
//        timeoutExecutor.shutdown();
//    }
//}
