package nl.javadude.t2bus.event.strategy;

import nl.javadude.t2bus.BusException;
import nl.javadude.t2bus.EventHandler;

import java.lang.reflect.InvocationTargetException;

public class ThrowingEventHandlerStrategy extends BaseEventHandlerStrategy {
    @Override
    protected void handleInvocationTargetException(Object event, EventHandler wrapper, InvocationTargetException e) {
        throw new BusException(e.getCause());
    }
}
