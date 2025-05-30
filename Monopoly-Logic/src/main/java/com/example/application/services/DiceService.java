//package com.example.application.services;
//
//import io.lettuce.core.api.StatefulRedisConnection;
//import io.lettuce.core.api.sync.RedisCommands;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DiceService {
//    private final StatefulRedisConnection<String, String> redisConnection;
//    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
//    private final Map<UUID, CompletableFuture<Integer>> diceResults = new ConcurrentHashMap<>();
//
//    public CompletableFuture<Integer> rollDice(UUID gameId) {
//        CompletableFuture<Integer> future = new CompletableFuture<>();
//        diceResults.put(gameId, future);
//
//        RedisCommands<String, String> sync = redisConnection.sync();
//        String channel = "game:" + gameId + ":dice-roll-action";
//        sync.publish(channel, "roll");
//
//        scheduler.schedule(() -> {
//            if (!future.isDone()) {
//                future.completeExceptionally(new TimeoutException("Dice roll timed out"));
//                diceResults.remove(gameId);
//            }
//        }, 90, TimeUnit.SECONDS);
//
//        return future;
//    }
//
//    public void completeDiceRoll(UUID gameId, int topFace) {
//        CompletableFuture<Integer> future = diceResults.remove(gameId);
//        if (future != null) {
//            future.complete(topFace);
//        }
//    }
//}