package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

public class NDetachedErrorOptionalException extends RuntimeException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    public NDetachedErrorOptionalException(NMsg formattedMessage,Throwable cause) {
        super(formattedMessage == null ? "error optional" : formattedMessage.toString(),cause);
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("error") : formattedMessage;
    }

    public NMsg getFormattedMessage() {
        return formattedMessage;
    }
}
