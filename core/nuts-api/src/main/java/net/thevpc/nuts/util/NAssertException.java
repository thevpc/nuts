package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NAssertException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NAssertException extends NIllegalArgumentException {

    /**
     * N assert exception.
     *
     * @param formattedMessage formatted message
     * @param cause cause
     * @return n assert exception result
     */
    public NAssertException(NMsg formattedMessage, Throwable cause) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       * @param cause cause
       */
        super(formattedMessage, cause);
    }

}
