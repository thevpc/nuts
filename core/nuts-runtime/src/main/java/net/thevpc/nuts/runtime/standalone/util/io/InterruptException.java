package net.thevpc.nuts.runtime.standalone.util.io;

public class InterruptException extends RuntimeException {
    public InterruptException() {
    }

    public InterruptException(String message) {
        super(message);
    }

    public InterruptException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptException(Throwable cause) {
        super(cause);
    }

    public InterruptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
