package net.vpc.app.nuts;

public class NutsIOException extends NutsException{
    public NutsIOException() {
    }

    public NutsIOException(String message) {
        super(message);
    }

    public NutsIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsIOException(Throwable cause) {
        super(cause);
    }

    public NutsIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
