package nl.javadude.t2bus;

import static java.lang.String.format;

/**
 * Exception thrown by a method that is annotated with {@link Subscribe} which has {@link nl.javadude.t2bus.Subscribe#canVeto()}
 * set to true.
 *
 * @author Jeroen van Erp, jeroen@javadude.nl
 */
public final class VetoException extends Exception {
    public VetoException(String messageFormat, Object... params) {
        this(format(messageFormat, params));
    }

    public VetoException(Throwable cause, String messageFormat, Object... params) {
        this(format(messageFormat, params), cause);
    }

    public VetoException() {
    }

    public VetoException(String message) {
        super(message);
    }

    public VetoException(String message, Throwable cause) {
        super(message, cause);
    }

    public VetoException(Throwable cause) {
        super(cause);
    }
}
