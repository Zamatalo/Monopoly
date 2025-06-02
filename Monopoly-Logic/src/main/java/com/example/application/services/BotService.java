// BotService.java
package com.example.application.services;

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
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BotService {
    private final MonopolyLLMBot llmBot;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public BotService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                      RedisService redisService,
                      ObjectMapper objectMapper) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.redisService = redisService;
        this.objectMapper = objectMapper;

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434/")
                .modelName("llama3")
                .timeout(Duration.ofSeconds(120))
                .build();

        this.llmBot = AiServices.create(MonopolyLLMBot.class, model);
    }

    @PostConstruct
    public void subscribeToGameUpdates() {
        reactiveRedisTemplate.listenToChannel("game:gameUpdate")
                .map(ReactiveSubscription.Message::getMessage)
                .cast(GameDTO.class)
                .subscribe(this::handleGameUpdate);
    }

    public void handleGameUpdate(GameDTO gameDTO) {
        try {
            PlayerDTO currentPlayer = getCurrentPlayer(gameDTO);
            if (currentPlayer.getIsBot()) {
                log.info("Bot turn for player: {}", currentPlayer.getPlayerName());
                String gameStateJson = "Game state: " + objectMapper.writeValueAsString(gameDTO);

                decideMoveAsync(gameStateJson)
                        .flatMap(action ->
                                reactiveRedisTemplate.opsForStream().add("game.request",
                                        Map.of(
                                                "action", action,
                                                "gameId", gameDTO.getGameId(),
                                                "playerId", currentPlayer.getPlayerId(),
                                                "correlationId", UUID.randomUUID(),
                                                "sentFromBot", true
                                        )
                                )
                        )
                        .subscribe();
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize gameDTO for bot decision", e);
        }
    }

    private Mono<String> decideMoveAsync(String gameStateJson) {
        return Mono.fromCallable(() -> {
            log.debug("Sending to LLM: {}", gameStateJson);
            String response = llmBot.decideAction(gameStateJson);
            log.debug("Received from LLM: {}", response);
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    private PlayerDTO getCurrentPlayer(GameDTO gameDTO) {
        return gameDTO.getPlayers().get(gameDTO.getCurrentPlayerIndex());
    }

    public interface MonopolyLLMBot {

        @SystemMessage("You are a bot playing Monopoly. You receive the full game state as JSON and possible actions. " +
                "Respond *only* with the exact action name as a plain string, like ROLL_DICE, BUY_PROPERTY, or END_TURN. " +
                "Do not include quotes, explanations, or any other text.")
        String decideAction(String gameStateJson);
    }
}