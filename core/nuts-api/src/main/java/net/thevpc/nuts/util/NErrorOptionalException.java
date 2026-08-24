package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NErrorOptionalException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NErrorOptionalException extends NException {

    /**
     * N error optional exception.
     *
     * @param formattedMessage formatted message
     * @param cause cause
     * @return n error optional exception result
     */
    public NErrorOptionalException(NMsg formattedMessage, Throwable cause) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       * @param cause cause
       */
        super(formattedMessage, cause);
    }

}
