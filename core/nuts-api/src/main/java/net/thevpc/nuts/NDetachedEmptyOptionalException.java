package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

public class NDetachedEmptyOptionalException extends RuntimeException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    public NDetachedEmptyOptionalException(NMsg formattedMessage) {
        super(formattedMessage == null ? "empty optional" : formattedMessage.toString());
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("empty") : formattedMessage;
    }

    public NMsg getFormattedMessage() {
        return formattedMessage;
    }
}
