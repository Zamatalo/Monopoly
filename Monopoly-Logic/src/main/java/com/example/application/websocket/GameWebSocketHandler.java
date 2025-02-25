//package com.example.application.websocket;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//@Component
//public class GameWebSocketHandler extends TextWebSocketHandler {
//
//    private final SimpMessagingTemplate template;
//
//    public GameWebSocketHandler(SimpMessagingTemplate template) {
//        this.template = template;
//    }
//
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        template.convertAndSend("/topic/game", message.getPayload());
//    }
//}
