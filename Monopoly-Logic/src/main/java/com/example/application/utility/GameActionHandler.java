package com.example.application.utility;

import com.example.application.components.RequestContextRedis;
import reactor.core.publisher.Mono;

public interface GameActionHandler {
    String getAction();
    Mono<Void> handle(RequestContextRedis ctx);
}
