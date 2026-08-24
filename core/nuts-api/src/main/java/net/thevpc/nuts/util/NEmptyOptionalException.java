package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NEmptyOptionalException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NEmptyOptionalException extends NException {

    /**
     * N empty optional exception.
     *
     * @param formattedMessage formatted message
     * @return n empty optional exception result
     */
    public NEmptyOptionalException(NMsg formattedMessage) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       */
        super(formattedMessage);
    }

}
