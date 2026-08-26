package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

/**
 * NCmdLineException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCmdLineException extends NIllegalArgumentException {

    /**
     * N cmd line exception.
     *
     * @param formattedMessage formatted message
     * @param cause cause
     * @return n cmd line exception result
     */
    public NCmdLineException(NMsg formattedMessage, Throwable cause) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       * @param cause cause
       */
        super(formattedMessage, cause);
    }

    /**
     * N cmd line exception.
     *
     * @param formattedMessage formatted message
     * @return n cmd line exception result
     */
    public NCmdLineException(NMsg formattedMessage) {
      /**
       * Super.
       *
       * @param formattedMessage formatted message
       */
        super(formattedMessage);
    }

}
