//package com.example.application.endpoints;
//
//import com.example.application.service.GameProxyService;
//import com.example.application.types.GameDTO;
//import com.example.application.types.PlayerColors;
//import com.vaadin.flow.server.auth.AnonymousAllowed;
//import com.vaadin.hilla.Endpoint;
//import lombok.RequiredArgsConstructor;
//
//import java.util.List;
//
//@Endpoint
//@RequiredArgsConstructor
//public class GraphqlEndpoint {
//    private final GameProxyService gameProxyService;
//
//    @AnonymousAllowed
//    public List<GameDTO> getActiveGames() {
//        return gameProxyService.fetchActiveGames();
//    }
//    @AnonymousAllowed
//    public GameDTO joinGame(String gameId, String playerColor,String playerName) {
//        return gameProxyService.joinGame(playerName, gameId, PlayerColors.valueOf(playerColor));
//    }
//}