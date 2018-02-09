package net.vpc.app.nuts;

public class NutsElementAlreadyDefinedException extends NutsException{
    public NutsElementAlreadyDefinedException() {
    }

    public NutsElementAlreadyDefinedException(String message) {
        super(message);
    }

    public NutsElementAlreadyDefinedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsElementAlreadyDefinedException(Throwable cause) {
        super(cause);
    }

    public NutsElementAlreadyDefinedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
