package com.example.application.utility;

import java.util.Map;

public interface GameActionHandler {
    String getAction();

    void handle(RequestContextRedis ctx);

}
