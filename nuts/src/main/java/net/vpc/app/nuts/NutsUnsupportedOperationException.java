package net.vpc.app.nuts;

public class NutsUnsupportedOperationException extends NutsException {
    public NutsUnsupportedOperationException() {
    }

    public NutsUnsupportedOperationException(String message) {
        super(message);
    }

    public NutsUnsupportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsUnsupportedOperationException(Throwable cause) {
        super(cause);
    }

    public NutsUnsupportedOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
