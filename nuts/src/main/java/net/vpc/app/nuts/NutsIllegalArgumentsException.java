package net.vpc.app.nuts;

public class NutsIllegalArgumentsException extends NutsException{
    public NutsIllegalArgumentsException() {
    }

    public NutsIllegalArgumentsException(String message) {
        super(message);
    }

    public NutsIllegalArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsIllegalArgumentsException(Throwable cause) {
        super(cause);
    }

    public NutsIllegalArgumentsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
