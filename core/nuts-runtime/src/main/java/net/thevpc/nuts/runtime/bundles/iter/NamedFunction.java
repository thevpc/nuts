package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

public class NamedFunction<F, T> implements Function<F, T>, IterInfoNodeAware {
    private final Function<F, T> converter;
    private final String name;

    public NamedFunction(Function<F, T> converter, String name) {
        this.converter = converter;
        this.name = name;
    }

    @Override
    public T apply(F f) {
        return converter.apply(f);
    }

    @Override
    public String toString() {
        return name == null ? (converter == null ? "null" : converter.toString()) : name;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return IterInfoNode.ofLiteralType("NamedFunction", name,null, name,
                IterInfoNode.resolveOrNull("function", converter, session)
        );
    }
}
