package net.thevpc.nuts.cmdline;

import java.util.function.Consumer;

public interface NCmdLineArgProcessor {
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

    boolean nextTrueFlag(Consumer<NArg> consumer);

}
