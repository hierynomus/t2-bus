package nl.javadude.t2bus.event.strategy;

import nl.javadude.t2bus.EventHandler;
import nl.javadude.t2bus.EventHandlerStrategy;
import nl.javadude.t2bus.VetoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public abstract class BaseEventHandlerStrategy implements EventHandlerStrategy {
    @Override
    public boolean handle(Object event, EventHandler wrapper) {
        try {
            wrapper.handleEvent(event);
        } catch (VetoException e) {
            return handleVetoException(event, wrapper, e);
        } catch (InvocationTargetException e) {
            handleInvocationTargetException(event, wrapper, e);
        }
        return true;
    }

    protected void handleInvocationTargetException(Object event, EventHandler wrapper, InvocationTargetException e) {
        if (wrapper.isVetoer()) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    protected boolean handleVetoException(Object event, EventHandler wrapper, VetoException e) {
        if (wrapper.isVetoer()) {
            logger.error("Event " + event + " was vetoed by handler " + wrapper, e);
            return false;
        }
        throw new Error("non-vetoer " + wrapper + " should not be able to throw a VetoException", e);
    }

    private static final Logger logger = LoggerFactory.getLogger(BaseEventHandlerStrategy.class);
}
