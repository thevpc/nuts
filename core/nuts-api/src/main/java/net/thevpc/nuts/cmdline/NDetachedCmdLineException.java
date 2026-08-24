package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NDetachedFormattedExceptionBase;
import net.thevpc.nuts.text.NMsg;

/**
 * NDetachedCmdLineException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDetachedCmdLineException extends IllegalArgumentException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    /**
     * N detached cmd line exception.
     *
     * @param formattedMessage formatted message
     * @param ex ex
     * @return n detached cmd line exception result
     */
    public NDetachedCmdLineException(NMsg formattedMessage, Throwable ex) {
      /**
       * Super.
       *
       * @param formattedMessage.toString() formatted message.to string()
       * @param ex ex
       */
        super(formattedMessage == null ? "assert failed" : formattedMessage.toString(),ex);
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("assert failed") : formattedMessage;
    }
    /**
     * N detached cmd line exception.
     *
     * @param formattedMessage formatted message
     * @return n detached cmd line exception result
     */
    public NDetachedCmdLineException(NMsg formattedMessage) {
      /**
       * Super.
       *
       * @param formattedMessage.toString() formatted message.to string()
       */
        super(formattedMessage == null ? "assert failed" : formattedMessage.toString());
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("assert failed") : formattedMessage;
    }

    /**
     * Formatted message.
     *
     * @return formatted message result
     */
    public NMsg formattedMessage() {
        return formattedMessage;
    }
}
