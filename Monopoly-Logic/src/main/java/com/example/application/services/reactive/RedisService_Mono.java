//package com.example.application.services.reactive;
//
//import com.example.application.types.GameDTO;
//import com.example.application.util.exceptions.RedisResponseException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.connection.stream.*;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.stream.StreamReceiver;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RedisService_Mono {
//    private final ReactiveRedisTemplate<String, String> redisTemplate;
//    private final StreamReceiver<String,MapRecord<String,String,String>> streamReceiver;
//    private final ReactiveRedisTemplate<String,Object> redisTemplate_forObjects;
//
//    @Value("${spring.data.redis.gameRequestStream}")
//    private String REQUEST_STREAM;
//    @Value("${spring.data.redis.gameResponseStream}")
//    private String RESPONSE_STREAM;
//    @Value("${spring.data.redis.backendGroup}")
//    private String BACKEND_GROUP;
//
//    @Value("${spring.data.redis.gameUpdateChannel}")
//    private String GAME_UPDATE_CHANNEL;
//
//    private final String CONSUMER_NAME = "backend-0";
//
//    /// helper functions to post request to backend(this service)
//    /// @see com.example.application.redis.GameRequestStreamListener
//    public Mono<RecordId> publishToRequestStream(Map<String, String> payload) {
//        return publishToStream(REQUEST_STREAM, payload, "Request");
//    }
//
//    /// helper functions to post response back to gateway
//    public Mono<RecordId> publishToResponseStream(Map<String, String> payload) {
//        return publishToStream(RESPONSE_STREAM, payload, "Response");
//    }
//
//    /// for creating group on startup, if they don't exist
//    public Mono<Void> ensureConsumerGroupExists() {
//        return redisTemplate.opsForStream()
//                .createGroup(REQUEST_STREAM, BACKEND_GROUP)
//                .onErrorResume(_ -> Mono.empty())
//                .then();
//    }
//
//    /// Redis stream messages should be ack
//    public Mono<Void> acknowledgeMessage(RecordId recordId) {
//        return redisTemplate
//                .opsForStream()
//                .acknowledge(REQUEST_STREAM, BACKEND_GROUP, recordId).then();
//    }
//
//    /// Subscribe to read from stream
//    public Flux<MapRecord<String, String, String>> listenToStream() {
//        return streamReceiver.receive(
//                Consumer.from(BACKEND_GROUP, CONSUMER_NAME),
//                StreamOffset.create(REQUEST_STREAM, ReadOffset.lastConsumed())
//        );
//    }
//
//    public Mono<Void> publishGameUpd(GameDTO gameDTO) {
//        try {
//            return redisTemplate_forObjects
//                    .convertAndSend(GAME_UPDATE_CHANNEL,gameDTO)
//                    .then();
//        }catch (Exception e){
//            log.error("Error when publishing Game Update: {}",e.getMessage());
//           return Mono.error(e);
//        }
//    }
//
//    /// Will publish to specified Stream
//    private Mono<RecordId> publishToStream(String stream, Map<String, String> payload, String logLabel) {
//        return redisTemplate.opsForStream()
//                .add(stream, payload)
//                .doOnSuccess(_ -> log.debug("{} sent: {}", logLabel, payload))
//                .doOnError(e -> log.error("Failed to send {}: {}", logLabel, payload, e))
//                .onErrorMap(e -> new RedisResponseException("Failed to send " + logLabel, e));
//    }
//
//}