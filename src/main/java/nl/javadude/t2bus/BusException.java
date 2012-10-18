package nl.javadude.t2bus;

public class BusException extends RuntimeException {
    public BusException() {
    }

    public BusException(String s) {
        super(s);
    }

    public BusException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BusException(Throwable throwable) {
        super(throwable);
    }
}
