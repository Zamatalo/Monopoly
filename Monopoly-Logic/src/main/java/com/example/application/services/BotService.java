package com.example.application.services;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Service;

@Service
public class BotService {
    private final MonopolyLLMBot llmBot;

    public BotService() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://host.docker.internal:11434")
                .modelName("llama3")
                .build();

        this.llmBot = AiServices.create(MonopolyLLMBot.class, model);
    }

    public String decideMove(String jsonState) {
        return llmBot.decideAction(jsonState);
    }


    public interface MonopolyLLMBot {

        @SystemMessage("You are a bot that plays a Monopoly game. You will receive the full game state as a JSON object. Respond *only* in JSON format. Your response must strictly follow this structure: {\"action\": \"yourChosenAction\"}, where \"yourChosenAction\" is one of the available legal actions provided in the game state.")
        String decideAction(String gameStateJson);
    }

}
