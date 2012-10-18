package nl.javadude.t2bus.event.strategy;

import java.lang.reflect.Method;

public interface ExceptionHandler {

    void handle(Throwable t, Object event, Object subscriber, Method handler);
}
