//package com.example.application.components;
//
//import org.springframework.web.reactive.socket.WebSocketHandler;
//import org.springframework.web.reactive.socket.WebSocketMessage;
//import org.springframework.web.reactive.socket.WebSocketSession;
//import reactor.core.publisher.Mono;
//
//import java.util.Arrays;
//import java.util.List;
//
///**
// * this, and WebsocketHandler should in the future communicate with API-Gateway
// * (Dice and Game publishers should later also be here)
// **/
//public class MySocketHandler implements WebSocketHandler {
//
//    @Override
//    public Mono<Void> handle(WebSocketSession session) {
//        return session.receive()
//                .map(WebSocketMessage::getPayloadAsText)
//                .doOnNext(message -> System.out.println("Received: " + message))
//                .flatMap(message -> session.send(
//                        Mono.just(session.textMessage("Echo: " + message))
//                ))
//                .then();
//    }
//
//    @Override
//    public List<String> getSubProtocols() {
//        return Arrays.asList("graphql-ws", "graphql-transport-ws");
//    }
//}