package net.vpc.app.nuts;

import java.io.IOException;

public class NutsException extends RuntimeException{
    public NutsException() {
    }

    public NutsException(String message) {
        super(message);
    }

    public NutsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsException(Throwable cause) {
        super(cause);
    }

    public NutsException(IOException cause) {
        super(cause);
    }

    public NutsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
