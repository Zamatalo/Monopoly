//package com.example.application.client;
//
//import com.example.application.dto.GameDTO;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//
//
//@FeignClient(name = "graphql-tasks", url = "localhost:8081" + GameClient.SUB_URL)
//public interface GameClient {
//    String SUB_URL = "/api/v1/graphql";
//
//    @PostMapping
//    GameDTO findGameById(String id);
//
/// /    @PostMapping
//    //  List<GameDTO> findAllGames(@RequestBody GraphQlRequest query);
//}