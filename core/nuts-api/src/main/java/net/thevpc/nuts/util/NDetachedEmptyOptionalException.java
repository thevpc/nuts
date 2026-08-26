package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NDetachedEmptyOptionalException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDetachedEmptyOptionalException extends RuntimeException implements NDetachedFormattedExceptionBase {
    private NMsg formattedMessage;

    /**
     * N detached empty optional exception.
     *
     * @param formattedMessage formatted message
     * @return n detached empty optional exception result
     */
    public NDetachedEmptyOptionalException(NMsg formattedMessage) {
      /**
       * Super.
       *
       * @param formattedMessage.toString() formatted message.to string()
       */
        super(formattedMessage == null ? "empty optional" : formattedMessage.toString());
        this.formattedMessage = formattedMessage == null ? NMsg.ofC("empty") : formattedMessage;
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
