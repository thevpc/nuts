package net.thevpc.nuts.net;

import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.text.NMsgCodeAware;

/**
 * NWebResponseException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NHttpResponseException extends NException implements NMsgCodeAware {
    public NHttpCode code;
    public NMsg responseMessage;
    public NMsgCode messageCode;

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #message()} method.
     */
    public NHttpResponseException(NMsg message, NMsgCode messageCode, NHttpCode code) {
      /**
       * Super.
       *
       * @param messageCode!=null?NMsg.ofC("%s",messageCode.message()):message message code!=null?n msg.of c("%s",message code.message()):message
       */
        super(messageCode!=null?NMsg.ofC("%s",messageCode.message()):message);
        this.code = code;
        this.responseMessage = message;
        this.messageCode = messageCode;
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <br>
     * Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #message()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public NHttpResponseException(NMsg message, NMsgCode messageCode, NHttpCode code, Throwable cause) {
      /**
       * Super.
       *
       * @param messageCode!=null?NMsg.ofC("%s",messageCode.message()):message message code!=null?n msg.of c("%s",message code.message()):message
       * @param cause cause
       */
        super(messageCode!=null?NMsg.ofC("%s",messageCode.message()):message, cause);
        this.code = code;
        this.responseMessage = message;
        this.messageCode = messageCode;
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     */
    public NHttpResponseException(NMsg message, NMsgCode messageCode, NHttpCode code, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      /**
       * Super.
       *
       * @param messageCode!=null?NMsg.ofC("%s",messageCode.message()):message message code!=null?n msg.of c("%s",message code.message()):message
       * @param cause cause
       * @param enableSuppression enable suppression
       * @param writableStackTrace writable stack trace
       */
        super(messageCode!=null?NMsg.ofC("%s",messageCode.message()):message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.responseMessage = message;
        this.messageCode = messageCode;
    }

    /**
     * Status code.
     *
     * @return status code result
     */
    public NHttpCode statusCode() {
        return code;
    }

    @Override
    public NMsgCode msgCode() {
        return messageCode;
    }

    /**
     * Response message.
     *
     * @return response message result
     */
    public NMsg responseMessage() {
        return responseMessage;
    }

}
