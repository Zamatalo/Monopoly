package com.example.application.util.exceptions;

public class RedisResponseException extends RuntimeException {
  public RedisResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
