package nl.javadude.t2bus;

import java.lang.reflect.Method;

public interface ExceptionHandler {

    void handle(Throwable t, Object event, Object subscriber, Method handler);
}
