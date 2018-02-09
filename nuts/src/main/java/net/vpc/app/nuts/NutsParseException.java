package net.vpc.app.nuts;

public class NutsParseException extends NutsException{
    public NutsParseException() {
    }

    public NutsParseException(String message) {
        super(message);
    }

    public NutsParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsParseException(Throwable cause) {
        super(cause);
    }

    public NutsParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
