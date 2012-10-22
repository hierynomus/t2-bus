package nl.javadude.t2bus.event.strategy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class LoggingEventHandlerStrategy extends ExceptionHandlerEventHandlerStrategy {
    public LoggingEventHandlerStrategy() {
        super(new LoggingExceptionHandler(logger));
    }

    static class LoggingExceptionHandler implements ExceptionHandler {
        private final Logger logger;

        LoggingExceptionHandler(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public void handle(final Throwable t, final Object event, final Object subscriber, final Method handler) {
            logger.error("Could not dispatch event: " + event + " to handler " + subscriber + "[" + handler.getName() + "]", t);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(LoggingEventHandlerStrategy.class);
}
