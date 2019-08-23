package net.vpc.app.nuts.core.util.iter;

import java.util.Iterator;

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
