package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NImmutable;

import java.util.function.Supplier;
/**
 * A wrapper around an {@link NUnsafeRunnable} that adds a custom descriptive element.
 * <p>
 * This class is immutable and delegates the execution of {@link #run()} to the underlying
 * {@link NUnsafeRunnable} while providing a custom description via a {@link Supplier} of {@link NElement}.
 *
 * @since 0.8.7
 */
public class NUnsafeRunnableWithDescription implements NUnsafeRunnable, NImmutable {
    /** The original unsafe runnable being wrapped. */
    private final NUnsafeRunnable base;

    /** Supplier that provides the description of this runnable. */
    private final Supplier<NElement> nfo;

    /**
     * Creates a new wrapper for the given unsafe runnable with a custom description.
     *
     * @param base the unsafe runnable to wrap
     * @param nfo  a supplier that returns the descriptive {@link NElement}
     */
    public NUnsafeRunnableWithDescription(NUnsafeRunnable base, Supplier<NElement> nfo) {
        this.base = base;
        this.nfo = nfo;
    }


    /**
     * Returns the descriptive {@link NElement} provided by the supplier.
     *
     * @return the description element
     */
    @Override
    public NElement describe() {
        return nfo.get();
    }

    /**
     * Executes the wrapped {@link NUnsafeRunnable}.
     *
     * @throws Exception if the underlying runnable throws an exception
     */
    @Override
    public void run() throws Exception {
        base.run();
    }
}
