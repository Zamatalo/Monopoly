package com.example.application.services;

import com.example.application.components.GameActionResolver;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class BotService {
    private final MonopolyLLMBot llmBot;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final ConcurrentLinkedQueue<GameDTO> updateQueue = new ConcurrentLinkedQueue<>();

    public BotService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                      RedisTemplate<String, String> redisTemplate,
                      ObjectMapper objectMapper) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434/")
                .modelName("llama3")
                .timeout(Duration.ofSeconds(120))
                .build();

        this.llmBot = AiServices.create(MonopolyLLMBot.class, model);
    }

    @PostConstruct
    public void init() {
        subscribeToGameUpdates();
        startQueueProcessor();
    }

    private void subscribeToGameUpdates() {
        Flux.merge(
                        reactiveRedisTemplate.listenToChannel("game:turnEnd"),
                        reactiveRedisTemplate.listenToChannel("game:botDecision"))
                .map(ReactiveSubscription.Message::getMessage)
                .cast(GameDTO.class)
                .subscribe(this::enqueueGameUpdate);
    }

    private void enqueueGameUpdate(GameDTO gameDTO) {
        updateQueue.add(gameDTO);
    }

    private void startQueueProcessor() {
        Flux.interval(Duration.ofMillis(5000))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(_ -> processQueue());
    }

    private void processQueue() {
        try {
            GameDTO gameDTO = updateQueue.poll();
            if (gameDTO != null) {
                handleGameUpdate(gameDTO);
            }
        } catch (Exception e) {
            log.error("Error processing game update from queue", e);
        }
    }

    public void handleGameUpdate(GameDTO gameDTO) {
        PlayerDTO currentPlayer = getCurrentPlayer(gameDTO);
        if (Boolean.TRUE.equals(currentPlayer.getIsBot())) {

            log.info("Bot turn for player: {}", currentPlayer.getPlayerName());
            try {
                var possibleActions = GameActionResolver.resolvePlayerActions(gameDTO,currentPlayer);
                String gameStateJson = objectMapper.writeValueAsString(gameDTO);

                decideMoveAsync(gameStateJson)
                        .doOnNext(action -> sendBotAction(gameDTO, currentPlayer, action))
                        .subscribe();
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize gameDTO for bot decision", e);
            }
        }
    }

    private void sendBotAction(GameDTO gameDTO, PlayerDTO player, String action) {
        Map<String, String> body = Map.of(
                "action", action,
                "gameId", gameDTO.getGameId(),
                "playerId", player.getPlayerId(),
                "correlationId", UUID.randomUUID().toString(),
                "sentFromBot", "true"
        );

        StringRecord record = StreamRecords.string(body).withStreamKey("game.request");
        redisTemplate.opsForStream().add(record);
    }

    private Mono<String> decideMoveAsync(String gameStateJson) {
        return Mono.fromCallable(() -> llmBot.decideAction(gameStateJson)
                .trim()).subscribeOn(Schedulers.boundedElastic());
    }

    private PlayerDTO getCurrentPlayer(GameDTO gameDTO) {
        return gameDTO.getPlayers().get(gameDTO.getCurrentPlayerIndex());
    }

    public interface MonopolyLLMBot {
        @SystemMessage(
                "You are a bot playing Monopoly. You receive the full game state as JSON, including a list of possible actions. " +
                        "Choose exactly one valid action from the list and respond *only* with that action as a plain string. " +
                        "Do not include quotes, explanations, or any other text."
        )
        String decideAction(String gameStateJson);
    }
}