package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NDetachedAssertException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDetachedAssertException extends IllegalArgumentException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    /**
     * N detached assert exception.
     *
     * @param formattedMessage formatted message
     * @param ex ex
     * @return n detached assert exception result
     */
    public NDetachedAssertException(NMsg formattedMessage,Throwable ex) {
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
     * N detached assert exception.
     *
     * @param formattedMessage formatted message
     * @return n detached assert exception result
     */
    public NDetachedAssertException(NMsg formattedMessage) {
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
