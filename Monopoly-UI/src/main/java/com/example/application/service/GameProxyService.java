//package com.example.application.service;
//
//import com.example.application.types.GameDTO;
//import com.example.application.types.PlayerColors;
//import lombok.extern.slf4j.Slf4j;
//import org.intellij.lang.annotations.Language;
//import org.springframework.graphql.client.HttpGraphQlClient;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//@Slf4j
//@Service
//public class GameProxyService {
//    private final WebClient webClient = WebClient.create("http://localhost:8081/api/v1/graphql");
//    private final HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient);
//
//    public List<GameDTO> fetchActiveGames() {
//        @Language("GraphQL")
//        String query = """
//                 query {
//                   getActiveGames {
//                     gameId
//                     players {
//                         playerId
//                     }
//                   }
//                 }
//                """;
//        return graphQlClient.document(query)
//                .retrieve("getActiveGames")
//                .toEntityList(GameDTO.class)
//                .doOnNext(games -> log.info("Active games found: {}", games))
//                .block();
//    }
//
//    public GameDTO joinGame(String playerName, String gameId, PlayerColors playerColor) {
//        @Language("GraphQl")
//        String query = """
//                subscription joinGame($playerName: String!, $gameId: ID!, $playerColor: PlayerColors!) {
//                     joinToGame(gameId: $gameId, playerName: $playerName, playerColor: $playerColor) {
//                         gameId
//                         currentPlayerIndex
//                         gameState
//                         createdTime
//                         players {
//                             playerId
//                             color
//                             balance
//                             position
//                             name
//                             inJail
//                         }
//                     }
//                 }
//                """;
//
//        Map<String, Object> variables = Map.of(
//                "playerName", playerName,
//                "gameId", gameId,
//                "playerColor", playerColor
//        );
//
//        var games = graphQlClient.document(query)
//                .variables(variables)
//                .executeSubscription();
//        log.info("Active games found: {}", games);
//        return games.blockFirst().field("joinToGame").toEntity(GameDTO.class);
//
//    }
//}
