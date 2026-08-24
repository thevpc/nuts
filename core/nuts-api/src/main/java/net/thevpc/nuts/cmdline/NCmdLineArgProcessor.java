package net.thevpc.nuts.cmdline;

import java.util.function.Consumer;

/**
 * NCmdLineArgProcessor interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCmdLineArgProcessor {
    /**
     * Checks if is acceptable.
     *
     * @return is acceptable result
     */
    boolean isAcceptable();

    /**
     * consume next argument with boolean value and run {@code consumer}
     *
     * @return true if active
     */
    boolean nextFlag(Consumer<NArg> consumer);

    /**
     * consume next argument with string value and run {@code consumer}
     *
     * @return true if active
     */
    boolean nextEntry(Consumer<NArg> consumer);

    /**
     * Next true flag.
     *
     * @param consumer consumer
     * @return next true flag result
     */
    boolean nextTrueFlag(Consumer<NArg> consumer);

}
