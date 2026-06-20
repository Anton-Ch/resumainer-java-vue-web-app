package com.resumainer.pdfspike.db;

public record ConnectionPoolConfig(String jdbcUrl, int maxSize) {}
