package net.thevpc.nuts.runtime.standalone.util.iter;

import java.util.Iterator;

public abstract class NamedIterator<T> implements Iterator<T> {
    private String name;

    public NamedIterator(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
