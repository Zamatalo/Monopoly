//package com.example.application.service;
//
//import com.example.application.endpointse.RedisEndpoint;
//import com.example.application.types.GameDTO;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RedisMessageSubscriber implements MessageListener {
//
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        try {
//            String channel = new String(message.getChannel());
//            String payload = new String(message.getBody());
//            log.debug("Received message on channel {}: {}", channel, payload);
//
//            String eventType = channel.substring(channel.lastIndexOf(':') + 1);
//            JsonNode node = objectMapper.readTree(payload);
//
//
//        } catch (Exception e) {
//            log.error("Error processing message: {}", message, e);
//        }
//    }
//}
