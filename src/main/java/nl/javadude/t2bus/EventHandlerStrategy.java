package nl.javadude.t2bus;

public interface EventHandlerStrategy {
    boolean handle(Object event, EventHandler wrapper);
}
