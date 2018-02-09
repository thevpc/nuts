package net.vpc.app.nuts;

public class NutsLoginException extends NutsException{
    public NutsLoginException() {
    }

    public NutsLoginException(String message) {
        super(message);
    }

    public NutsLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsLoginException(Throwable cause) {
        super(cause);
    }

    public NutsLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
