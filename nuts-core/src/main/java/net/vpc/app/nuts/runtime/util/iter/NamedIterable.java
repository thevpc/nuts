package net.vpc.app.nuts.runtime.util.iter;

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
