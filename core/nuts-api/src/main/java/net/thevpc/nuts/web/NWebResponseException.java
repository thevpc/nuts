package net.thevpc.nuts.web;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NMsgCodeAware;

public class NWebResponseException extends NException implements NMsgCodeAware {
    public NHttpCode code;
    public NMsg responseMessage;
    public NMsgCode messageCode;

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public NWebResponseException(NMsg message, NMsgCode messageCode, NHttpCode code) {
        super(messageCode!=null?NMsg.ofC("%s",messageCode.getMessage()):message);
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
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public NWebResponseException(NMsg message, NMsgCode messageCode, NHttpCode code, Throwable cause) {
        super(messageCode!=null?NMsg.ofC("%s",messageCode.getMessage()):message, cause);
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
    public NWebResponseException(NMsg message, NMsgCode messageCode, NHttpCode code, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(messageCode!=null?NMsg.ofC("%s",messageCode.getMessage()):message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.responseMessage = message;
        this.messageCode = messageCode;
    }

    public NHttpCode getCode() {
        return code;
    }

    @Override
    public NMsgCode getMsgCode() {
        return messageCode;
    }

    public NMsg getResponseMessage() {
        return responseMessage;
    }

}
