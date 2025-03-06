package com.example.application.service;

import com.example.application.client.FindGameByIdGraphQLQuery;
import com.example.application.client.FindGameByIdProjectionRoot;
import com.example.application.client.GameUpdatedGraphQLQuery;
import com.example.application.client.RollDiceGraphQLQuery;
import com.example.application.types.GameDTO;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.WebSocketGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
public class GameService {
    private final WebClient webClient = WebClient.create("http://localhost:8081/api/v1/graphql");
    private final HttpGraphQlClient httpClient = HttpGraphQlClient.create(webClient);
    private final DgsGraphQlClient dgsClient = DgsGraphQlClient.create(httpClient);

    private final WebSocketClient wsClient = new ReactorNettyWebSocketClient();
    private final WebSocketGraphQlClient wsGraphQlClient = WebSocketGraphQlClient.builder(URI.create("ws://localhost:8081/api/v1/graphql"), wsClient)
            .build();

    public GameDTO findGameById(UUID gameId) {
        FindGameByIdGraphQLQuery findGameByIdGraphQLQuery = FindGameByIdGraphQLQuery.newRequest()
                .id(gameId.toString())
                .build();

        var projection = new FindGameByIdProjectionRoot<>()
                .gameId()
                .currentPlayerIndex()
                .gameState().parent()
                .players()
                .playerId()
                .color().parent()
                .balance()
                .position()
                .ownedProperties()
                .propertyName().parent()
                .cost()
                .rent()
                .upgradable();

        try {
            return dgsClient.request(findGameByIdGraphQLQuery)
                    .projection(projection)
                    .retrieveSync("findGameById")
                    .toEntity(GameDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch game with ID: {}", gameId, e);
            throw new RuntimeException("Failed to fetch game: " + e.getMessage(), e);
        }
    }

    public Flux<GameDTO> subscribeToGame(UUID gameId) {
        GameUpdatedGraphQLQuery gameUpdatedGraphQLQuery = GameUpdatedGraphQLQuery.newRequest()
                .gameId(gameId.toString())
                .build();

        var projection = new FindGameByIdProjectionRoot<>()
                .gameId()
                .currentPlayerIndex()
                .gameState().parent()
                .players()
                .playerId()
                .color().parent()
                .balance()
                .position()
                .ownedProperties()
                .propertyName().parent()
                .cost()
                .rent()
                .upgradable();

        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(gameUpdatedGraphQLQuery, projection);
        return wsGraphQlClient.document(graphQLQueryRequest.serialize())
                .retrieveSubscription("gameUpdated")
                .toEntity(GameDTO.class)
                .doOnSubscribe(subscription -> log.info("Subscribed to game updates for game ID: {}", gameId))
                .doOnError(error -> log.error("Subscription error for game ID: {}", gameId, error));
    }

    public void rollDice(UUID gameId) {
        RollDiceGraphQLQuery rollDiceGraphQLQuery = RollDiceGraphQLQuery.newRequest()
                .gameId(gameId.toString())
                .build();

        log.info("Rolling dice for game: {}", gameId);
        try {
            dgsClient.request(rollDiceGraphQLQuery).retrieveSync();
        } catch (Exception e) {
            log.error("Failed to roll dice for game ID: {}", gameId, e);
            throw new RuntimeException("Failed to roll dice: " + e.getMessage(), e);
        }
    }
}