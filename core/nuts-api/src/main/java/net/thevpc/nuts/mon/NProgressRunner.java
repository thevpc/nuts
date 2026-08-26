package net.thevpc.nuts.mon;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Consumer;

/**
 * NProgressRunner interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NProgressRunner {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NProgressRunner of() {
        return  NIORPI.of().createProgressRunner();
    }

    /**
     * Adds add.
     *
     * @param processor processor
     * @param weight weight
     */
    void add(Consumer<Context> processor, double weight);

    /**
     * Adds add.
     *
     * @param processor processor
     */
    void add(Consumer<Context> processor);

    /**
     * Adds add.
     *
     * @param processor processor
     * @param weight weight
     */
    void add(Runnable processor, double weight);

    /**
     * Adds add.
     *
     * @param processor processor
     */
    void add(Runnable processor);


    /**
     * Run.
     */
    void run();

    interface Context {
        /**
         * Returns the get.
         *
         * @param name name
         * @return get result
         */
        <T> NOptional<T> get(String name);

        /**
         * Sets the set.
         *
         * @param name name
         * @param value value
         * @return set result
         */
        <T> Context set(String name, Object value);

        /**
         * Returns the get.
         *
         * @param name name
         * @param expectedType expected type
         * @return get result
         */
        <T> NOptional<T> get(String name, Class<T> expectedType);
    }
}
