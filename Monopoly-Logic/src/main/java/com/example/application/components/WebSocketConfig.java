//package com.example.application.components;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.HandlerMapping;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
//import org.springframework.web.reactive.socket.WebSocketHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class WebSocketConfig implements WebFluxConfigurer {
//
//    @Bean
//    public HandlerMapping webSocketHandlerMapping() {
//        Map<String, WebSocketHandler> map = new HashMap<>();
//        map.put("/api/v1/graphql", new MySocketHandler());
//
//        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
//        handlerMapping.setOrder(1);
//        handlerMapping.setUrlMap(map);
//        return handlerMapping;
//    }
//
//}