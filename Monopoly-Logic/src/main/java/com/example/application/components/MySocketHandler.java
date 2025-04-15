package com.example.application.components;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class MySocketHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .doOnNext(message -> System.out.println("Received: " + message))
                .flatMap(message -> session.send(
                        Mono.just(session.textMessage("Echo: " + message))
                ))
                .then();
    }

    @Override
    public List<String> getSubProtocols() {
        return Arrays.asList("graphql-ws", "graphql-transport-ws");
    }
}