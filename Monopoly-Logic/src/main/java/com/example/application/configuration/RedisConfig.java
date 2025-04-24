package com.example.application.configuration;

import com.example.application.redis.RedisMessageSubscriber;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private Integer port;

    @Bean
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.Builder.redis(host, port).build();
        return RedisClient.create(redisURI);
    }

    @Bean
    public StatefulRedisPubSubConnection<String, String> redisPubSubConnection(
            RedisClient redisClient,
            RedisMessageSubscriber messageSubscriber) {
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
        connection.addListener(messageSubscriber);
        connection.async().psubscribe("game:*");
        return connection;
    }
}