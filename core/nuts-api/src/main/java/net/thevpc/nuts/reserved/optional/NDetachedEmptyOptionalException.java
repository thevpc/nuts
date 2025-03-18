package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.NDetachedExceptionBase;
import net.thevpc.nuts.util.NMsg;

public class NDetachedEmptyOptionalException extends RuntimeException implements NDetachedExceptionBase {
    private NMsg formattedMessage;

    public NDetachedEmptyOptionalException(NMsg formattedMessage) {
        super(formattedMessage == null ? "empty optional" : formattedMessage.toString());
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("empty") : formattedMessage;
    }

    public NMsg getFormattedMessage() {
        return formattedMessage;
    }
}
