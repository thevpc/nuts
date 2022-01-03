package net.thevpc.nuts.runtime.standalone.xtra.cp;

import java.io.IOException;

public class MiddleTransferException extends IOException {
    public MiddleTransferException() {
    }

    public MiddleTransferException(String message) {
        super(message);
    }

    public MiddleTransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public MiddleTransferException(Throwable cause) {
        super(cause);
    }
}
