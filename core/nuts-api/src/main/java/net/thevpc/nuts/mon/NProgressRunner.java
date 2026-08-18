package net.thevpc.nuts.mon;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Consumer;

public interface NProgressRunner {
    static NProgressRunner of() {
        return  NIORPI.of().createProgressRunner();
    }

    void add(Consumer<Context> processor, double weight);

    void add(Consumer<Context> processor);

    void add(Runnable processor, double weight);

    void add(Runnable processor);


    void run();

    interface Context {
        <T> NOptional<T> get(String name);

        <T> Context set(String name, Object value);

        <T> NOptional<T> get(String name, Class<T> expectedType);
    }
}
