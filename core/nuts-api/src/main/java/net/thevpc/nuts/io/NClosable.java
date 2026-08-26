package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NException;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * NClosable interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NClosable extends Closeable {
    /**
     * Creates a new instance of any.
     *
     * @param any any
     * @return of any result
     */
    static NClosable ofAny(Object any) {
        if(any==null){
            return () -> {};
        }
        if(any instanceof NClosable){
            return (NClosable) any;
        }
        if(any instanceof AutoCloseable){
            return new NClosable() {
                @Override
                public void close() {
                    try {
                        ((AutoCloseable)any).close();
                    } catch (Exception e) {
                        throw NException.ofUncheckedException(e);
                    }
                }
            };
        }
        return () -> {};
    }

    /**
     * Do with.
     *
     * @param any any
     * @param r r
     * @return do with result
     */
    static <T> void doWith(T any, Consumer<T> r) {
      /**
       * Try.
       *
       * @param ofAny(any) of any(any)
       */
        try (NClosable ignored = ofAny(any)) {
            r.accept(any);
        }
    }

    /**
     * Call with.
     *
     * @param any any
     * @param r r
     * @return call with result
     */
    static <T, V> V callWith(T any, Function<T, V> r) {
      /**
       * Try.
       *
       * @param ofAny(any) of any(any)
       */
        try (NClosable ignored = ofAny(any)) {
            return r.apply(any);
        }
    }

    /**
     * Close.
     */
    void close();
}
