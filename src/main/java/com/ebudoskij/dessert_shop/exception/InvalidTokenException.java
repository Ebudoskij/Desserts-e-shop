package com.ebudoskij.dessert_shop.exception;

public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(String message) {
    super(message);
  }
}
