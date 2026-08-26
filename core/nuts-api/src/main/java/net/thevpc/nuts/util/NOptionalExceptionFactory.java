package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

/**
 * Factory interface used to customize the creation of exceptions thrown by
 * {@code NOptional} when terminal methods (like {@link NOptional#get()})
 * encounter a failure condition (Empty or Error), as well as exceptions
 * thrown by related utility classes (like assertion helpers).
 */
public interface NOptionalExceptionFactory {

    /**
     * Creates a {@code RuntimeException} to be thrown when a value is expected,
     * but the {@link NOptional} is in the **Empty** state.
     *
     * @param message the descriptive message for the empty state
     * @return the runtime exception to throw (e.g., NEmptyOptionalException)
     */
    RuntimeException createOptionalEmptyException(NMsg message);

    /**
     * Creates a {@code RuntimeException} to be thrown when a terminal method
     * is called on an optional that is in the **Error** state.
     *
     * @param message the descriptive error message
     * @param e       the underlying throwable that caused the error state, if available (may be null)
     * @return the runtime exception to throw (e.g., NErrorOptionalException)
     */
    RuntimeException createOptionalErrorException(NMsg message, Throwable e);

    /**
     * Creates a {@code RuntimeException} to be thrown for general **assertion failures**
     * within the Nuts framework (e.g., by the {@code NAssert} utility).
     *
     * @param message the assertion failure message
     * @param e       the underlying throwable, if available (may be null)
     * @return the runtime exception to throw
     */
    RuntimeException createAssertException(NMsg message, Throwable e);

    /**
     * Creates a {@code RuntimeException} specifically for **command-line related errors**,
     * typically encountered during option parsing, validation, or command execution.
     *
     * @param message the command-line error message
     * @param e       the underlying throwable, if available (may be null)
     * @return the runtime exception to throw
     */
    RuntimeException createCmdLineException(NMsg message, Throwable e);
}
