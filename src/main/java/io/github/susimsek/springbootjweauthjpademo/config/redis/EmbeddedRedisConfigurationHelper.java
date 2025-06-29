package io.github.susimsek.springbootjweauthjpademo.config.redis;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


@UtilityClass
@Slf4j
public class EmbeddedRedisConfigurationHelper {


    public Object createServer(int port) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            // Load RedisServer class from embedded-redis library
            Class<?> serverClass = Class.forName("redis.embedded.RedisServer", true, loader);
            // Find constructor taking int or String[] args
            Constructor<?> ctor = serverClass.getConstructor(int.class);
            Object server = ctor.newInstance(port);
            log.debug("Embedded Redis started on port {}", port);
            return server;
        } catch (ClassNotFoundException | LinkageError | NoSuchMethodException
                 | IllegalAccessException | InstantiationException e) {
            log.error("Failed to initialize embedded Redis server", e);
            throw new IllegalStateException("Failed to initialize embedded Redis server", e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof IOException ioEx) {
                throw new IllegalStateException("I/O error starting embedded Redis", ioEx);
            }
            log.error("Unexpected exception in RedisServer.start()", t);
            throw new IllegalStateException("Unexpected exception in RedisServer.start()", t);
        }
    }

    public int extractPort(Object redisServer) {
        try {
            Method portsMethod = redisServer.getClass().getMethod("ports");
            List<Integer> ports = (List<Integer>) portsMethod.invoke(redisServer);
            if (ports.isEmpty()) {
                throw new IllegalStateException("RedisServer.ports() returned empty list");
            }
            return ports.getFirst();
        } catch (Exception e) {
            log.error("Failed to extract port from embedded RedisServer", e);
            throw new IllegalStateException("Cannot get port from embedded RedisServer", e);
        }
    }
}
