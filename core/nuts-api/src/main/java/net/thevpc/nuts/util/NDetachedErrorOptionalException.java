package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NDetachedErrorOptionalException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDetachedErrorOptionalException extends RuntimeException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    /**
     * N detached error optional exception.
     *
     * @param formattedMessage formatted message
     * @param cause cause
     * @return n detached error optional exception result
     */
    public NDetachedErrorOptionalException(NMsg formattedMessage, Throwable cause) {
      /**
       * Super.
       *
       * @param formattedMessage.toString() formatted message.to string()
       * @param cause cause
       */
        super(formattedMessage == null ? "error optional" : formattedMessage.toString(),cause);
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("error") : formattedMessage;
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
