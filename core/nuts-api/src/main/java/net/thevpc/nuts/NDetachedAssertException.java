package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

public class NDetachedAssertException extends IllegalArgumentException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    public NDetachedAssertException(NMsg formattedMessage,Throwable ex) {
        super(formattedMessage == null ? "assert failed" : formattedMessage.toString(),ex);
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("assert failed") : formattedMessage;
    }
    public NDetachedAssertException(NMsg formattedMessage) {
        super(formattedMessage == null ? "assert failed" : formattedMessage.toString());
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("assert failed") : formattedMessage;
    }

    public NMsg getFormattedMessage() {
        return formattedMessage;
    }
}
