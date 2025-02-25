package com.example.application.service;


import com.example.application.client.FindGameByIdGraphQLQuery;
import com.example.application.client.FindGameByIdProjectionRoot;
import com.example.application.dto.GameDTO;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class GameService {
    private final WebClient webClient = WebClient.create("http://localhost:8081/api/v1/graphql");
    private final WebClientGraphQLClient client = MonoGraphQLClient.createWithWebClient(webClient);

    public GameService() {
    }

    public GameDTO findGameById(UUID gameId) {
        FindGameByIdGraphQLQuery findGameByIdGraphQLQuery = FindGameByIdGraphQLQuery.newRequest()
                .id(gameId.toString())
                .build();

        @Language("GraphQL")
        String query = new GraphQLQueryRequest(findGameByIdGraphQLQuery,
                new FindGameByIdProjectionRoot<>()
                        .gameId()
                        .currentPlayerIndex()
                        .gameState().parent()
                        .players()
                        .playerId()
                        .name().parent()
                        .balance()
                        .position()
                        .ownedProperties()
                        .propertyName().parent()
                        .cost()
                        .rent()
                        .upgradable()


        ).serialize();

        Mono<GraphQLResponse> graphQLResponseMono = client.reactiveExecuteQuery(query);

        return graphQLResponseMono
                .map(response -> response.extractValueAsObject("data.findGameById", GameDTO.class))
                .block();
    }

}
