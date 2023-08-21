package net.thevpc.nuts.web;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsg;

public class NWebResponseException extends NException {
    public int code;
    public NMsg responseMessage;
    public NMsg userMessage;

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param session the workspace of this Nuts Exception
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public NWebResponseException(NSession session, NMsg message, NMsg userMessage, int code) {
        super(session, userMessage != null ? userMessage : message);
        this.code = code;
        this.responseMessage = message;
        this.userMessage = userMessage;
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
     * @param session the workspace of this Nuts Exception
     */
    public NWebResponseException(NSession session, NMsg message, NMsg userMessage, int code, Throwable cause) {
        super(session, userMessage != null ? userMessage : message, cause);
        this.code = code;
        this.responseMessage = message;
        this.userMessage = userMessage;
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
     * @param session            the workspace of this Nuts Exception
     */
    public NWebResponseException(NSession session, NMsg message, NMsg userMessage, int code, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(session, userMessage != null ? userMessage : message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.responseMessage = message;
        this.userMessage = userMessage;
    }

    public int getCode() {
        return code;
    }

    public NMsg getResponseMessage() {
        return responseMessage;
    }

    public NMsg getUserMessage() {
        return userMessage;
    }
}
