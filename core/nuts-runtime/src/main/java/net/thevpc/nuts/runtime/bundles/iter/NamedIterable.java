package net.thevpc.nuts.runtime.bundles.iter;

public abstract class NamedIterable<T> implements Iterable<T> {
    private String name;

    public NamedIterable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
