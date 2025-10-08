package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NDetachedFormattedExceptionBase;
import net.thevpc.nuts.text.NMsg;

public class NDetachedCmdLineException extends IllegalArgumentException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    public NDetachedCmdLineException(NMsg formattedMessage, Throwable ex) {
        super(formattedMessage == null ? "assert failed" : formattedMessage.toString(),ex);
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("assert failed") : formattedMessage;
    }
    public NDetachedCmdLineException(NMsg formattedMessage) {
        super(formattedMessage == null ? "assert failed" : formattedMessage.toString());
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("assert failed") : formattedMessage;
    }

    public NMsg getFormattedMessage() {
        return formattedMessage;
    }
}
