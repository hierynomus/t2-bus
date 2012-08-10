package nl.javadude.t2bus;

/**
 * Thrown when something bad happens in the bus.
 */
class BusError extends Error {
    public BusError() {
    }

    public BusError(final String messageFormat, Object... params) {
        super(String.format(messageFormat, params));
    }

    public BusError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BusError(final Throwable cause) {
        super(cause);
    }
}
