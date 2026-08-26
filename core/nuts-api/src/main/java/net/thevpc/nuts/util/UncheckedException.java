package net.thevpc.nuts.util;

/**
 * UncheckedException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class UncheckedException extends RuntimeException{
    /**
     * Unchecked exception.
     *
     * @return unchecked exception result
     */
    public UncheckedException() {
    }

    /**
     * Unchecked exception.
     *
     * @param message message
     * @return unchecked exception result
     */
    public UncheckedException(String message) {
      /**
       * Super.
       *
       * @param message message
       */
        super(message);
    }

    /**
     * Unchecked exception.
     *
     * @param message message
     * @param cause cause
     * @return unchecked exception result
     */
    public UncheckedException(String message, Throwable cause) {
      /**
       * Super.
       *
       * @param message message
       * @param cause cause
       */
        super(message, cause);
    }

    /**
     * Unchecked exception.
     *
     * @param cause cause
     * @return unchecked exception result
     */
    public UncheckedException(Throwable cause) {
      /**
       * Super.
       *
       * @param cause cause
       */
        super(cause);
    }

    /**
     * Unchecked exception.
     *
     * @param message message
     * @param cause cause
     * @param enableSuppression enable suppression
     * @param writableStackTrace writable stack trace
     * @return unchecked exception result
     */
    public UncheckedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      /**
       * Super.
       *
       * @param message message
       * @param cause cause
       * @param enableSuppression enable suppression
       * @param writableStackTrace writable stack trace
       */
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
