package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * NNonCopiableException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NNonCopiableException extends NIllegalArgumentException {

    /**
     * N non copiable exception.
     *
     * @param formattedMessage formatted message
     * @param cause cause
     * @return n non copiable exception result
     */
    public NNonCopiableException(NMsg formattedMessage, Throwable cause) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       * @param cause cause
       */
        super(formattedMessage, cause);
    }
    /**
     * N non copiable exception.
     *
     * @param formattedMessage formatted message
     * @return n non copiable exception result
     */
    public NNonCopiableException(NMsg formattedMessage) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       */
        super(formattedMessage);
    }

}
