package com.example.application.utility;

public interface GameActionHandler {
    String getAction();

    void handle(RequestContextRedis ctx);

}
