package io.github.susimsek.springbootjweauthjpademo.config.logging;

public enum HttpLogLevel {
    NONE,
    BASIC,
    HEADERS,
    FULL;

    public boolean atLeast(HttpLogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
