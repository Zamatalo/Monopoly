package com.example.application.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GameConfig {
    public static final Integer START_PAYOUT = 200;
    public static final Integer INCOME_TAX = 200;

    public static String REQUEST_STREAM;
    public static String RESPONSE_STREAM;
    public static String GATEWAY_GROUP;
    public static String GAME_UPDATE_CHANNEL;
    public static String BACKEND_GROUP;
    public static String DICE_UPDATE_CHANNEL;

    //because @Value can't write into static param
    static {
        try (InputStream input = GameConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")
        ) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
            }
            prop.load(input);

            REQUEST_STREAM = prop.getProperty("spring.data.redis.gameRequestStream");
            RESPONSE_STREAM = prop.getProperty("spring.data.redis.gameResponseStream");
            GATEWAY_GROUP = prop.getProperty("spring.data.redis.gatewayGroup");
            GAME_UPDATE_CHANNEL = prop.getProperty("spring.data.redis.gameUpdateChannel");
            BACKEND_GROUP = prop.getProperty("spring.data.redis.backendGroup");
            DICE_UPDATE_CHANNEL = prop.getProperty("spring.data.redis.diceUpdateChannel");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
