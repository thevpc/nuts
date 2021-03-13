package net.thevpc.nuts.runtime.bundles.collections;

import java.util.LinkedList;

public class EvictingQueue<T> {
    int max;
    LinkedList<T> values = new LinkedList<>();

    public EvictingQueue(int max) {
        this.max = max;
    }

    public void clear() {
        values.clear();
    }

    public int size() {
        return values.size();
    }

    public T get(int pos) {
        return values.get(pos);
    }

    public void add(T t) {
        if (values.size() >= max) {
            values.removeFirst();
        }
        values.add(t);
    }

}
