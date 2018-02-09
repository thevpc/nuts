package net.vpc.app.nuts;

public class NutsElementNotFoundException extends NutsException{
    public NutsElementNotFoundException() {
    }

    public NutsElementNotFoundException(String message) {
        super(message);
    }

    public NutsElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsElementNotFoundException(Throwable cause) {
        super(cause);
    }

    public NutsElementNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
