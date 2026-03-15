package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.NExceptions;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface NClosable extends Closeable {
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
                        throw NExceptions.ofUncheckedException(e);
                    }
                }
            };
        }
        return () -> {};
    }

    static <T> void doWith(T any, Consumer<T> r) {
        try (NClosable ignored = ofAny(any)) {
            r.accept(any);
        }
    }

    static <T, V> V callWith(T any, Function<T, V> r) {
        try (NClosable ignored = ofAny(any)) {
            return r.apply(any);
        }
    }

    void close();
}
