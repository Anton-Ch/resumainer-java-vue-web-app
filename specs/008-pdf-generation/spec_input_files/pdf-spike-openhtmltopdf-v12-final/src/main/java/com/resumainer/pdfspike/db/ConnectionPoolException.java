package com.resumainer.pdfspike.db;

public class ConnectionPoolException extends RuntimeException {
    public ConnectionPoolException(String message, Throwable cause) { super(message, cause); }
    public ConnectionPoolException(String message) { super(message); }
}
