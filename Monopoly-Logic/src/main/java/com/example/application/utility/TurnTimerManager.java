//package com.example.application.utility;
//
//import com.example.application.components.GamePublisher;
//import com.example.application.entity.Game;
//import com.example.application.services.GameService;
//import com.example.application.types.GameDTO;
//import jakarta.annotation.PreDestroy;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.*;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class TurnTimerManager {
//    private final GameService gameService;
//    private final GamePublisher gamePublisher;
//    private final Map<UUID, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
//
//    public void startTurnTimer(UUID gameId) {
//        cancelExistingTimer(gameId);
//
//        ScheduledFuture<?> timer = scheduler.schedule(() -> {
//            try {
//                endTurnAutomatically(gameId);
//            } catch (Exception e) {
//                log.error("Failed to automatically end turn for game {}", gameId, e);
//            }
//        }, 90, TimeUnit.SECONDS);
//
//        activeTimers.put(gameId, timer);
//    }
//
//    private void endTurnAutomatically(UUID gameId) {
//        Game game = gameService.findById(gameId)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//        int nextPlayerIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();
//        game.setCurrentPlayerIndex(nextPlayerIndex);
//        gameService.save(game);
//
//        startTurnTimer(gameId);
//
//        GameDTO gameDTO = GameMapper.INSTANCE.GameToGameDTO(game);
//        gamePublisher.publish(gameDTO);
//
//        log.info("Turn automatically ended for game {}, next player: {}", gameId, nextPlayerIndex);
//    }
//
//    public void cancelExistingTimer(UUID gameId) {
//        ScheduledFuture<?> existingTimer = activeTimers.remove(gameId);
//        if (existingTimer != null && !existingTimer.isDone()) {
//            existingTimer.cancel(false);
//        }
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        activeTimers.values().forEach(timer -> timer.cancel(false));
//        scheduler.shutdown();
//    }
//}