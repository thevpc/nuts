package net.thevpc.nuts.util;

public abstract class NNamedIterable<T> implements Iterable<T> {
    private String name;

    public NNamedIterable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
