package net.thevpc.nuts.time;

import net.thevpc.nuts.util.NOptional;

import java.util.function.Consumer;

public interface NProgressRunner {
    static NProgressRunner of() {
        return NProgressMonitors.of().ofRunner();
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
