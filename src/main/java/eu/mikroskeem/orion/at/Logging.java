package eu.mikroskeem.orion.at;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * @author Mark Vainomaa
 */
final class Logging {
    private final static boolean hasLogger;
    private final static Map<Class<?>, Object> loggers = new WeakHashMap<>();

    static void trace(Class<?> clz, Supplier<String> message) {
        if(!hasLogger) return;
        Logger logger = (Logger) loggers.computeIfAbsent(clz, LoggerFactory::getLogger);
        if(logger.isDebugEnabled()) logger.trace(message.get());
    }

    static void debug(Class<?> clz, Supplier<String> message) {
        if(!hasLogger) return;
        Logger logger = (Logger) loggers.computeIfAbsent(clz, LoggerFactory::getLogger);
        if(logger.isDebugEnabled()) logger.debug(message.get());
    }

    static {
        boolean hasLogger1;
        try {
            Class.forName("org.slf4j.LoggerFactory");
            hasLogger1 = true;
        } catch (ClassNotFoundException ignored) {
            hasLogger1 = false;
        }
        hasLogger = hasLogger1;
    }
}
