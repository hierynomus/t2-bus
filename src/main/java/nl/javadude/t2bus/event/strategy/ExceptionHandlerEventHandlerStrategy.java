package nl.javadude.t2bus.event.strategy;

import nl.javadude.t2bus.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class ExceptionHandlerEventHandlerStrategy extends BaseEventHandlerStrategy {

    private ExceptionHandler exceptionHandler;

    public ExceptionHandlerEventHandlerStrategy(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void handleInvocationTargetException(Object event, EventHandler wrapper, InvocationTargetException e) {
        handleException(event, wrapper, e);
        super.handleInvocationTargetException(event, wrapper, e);
    }

    private void handleException(final Object event, final EventHandler wrapper, final InvocationTargetException e) {
        try {
            exceptionHandler.handle(e.getCause(), event, wrapper.getTarget(), wrapper.getMethod());
        } catch (Exception ex) {
            logger.error("Error occurred when handling exception from [{}] with event [{}]", wrapper.getMethod(), event);
            logger.error("Exception that was being handled: ", e.getCause());
            logger.error("Exception that occurred: ", ex);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerEventHandlerStrategy.class);
}
