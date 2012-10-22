package nl.javadude.t2bus.event.strategy;

import nl.javadude.t2bus.BusException;
import nl.javadude.t2bus.EventHandler;

import java.lang.reflect.InvocationTargetException;

public class ThrowingRuntimeExceptionHandlerStrategy extends BaseEventHandlerStrategy {
    @Override
    protected void handleInvocationTargetException(Object event, EventHandler wrapper, InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw new BusException(cause);
    }
}
