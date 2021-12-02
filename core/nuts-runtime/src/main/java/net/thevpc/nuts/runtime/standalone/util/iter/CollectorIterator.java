package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsDescribables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CollectorIterator<T> extends NutsIteratorBase<T> {

    private String name;
    private Iterator<T> base;
    private List<T> collected = new ArrayList<>();

    public CollectorIterator(String name, Iterator<T> base) {
        this.name = name;
        this.base = base;
    }

    @Override
    public NutsElement describe(NutsElements elems) {
        return elems.ofObject()
                .set("type", "Collector")
                .set("name", name)
                .set("base", NutsDescribables.resolveOrDestruct(base, elems))
                .build();
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        T x = base.next();
        collected.add(x);
        return x;
    }

    public List<T> getCollected() {
        return collected;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "collector(" + base + ")";
        }
        return String.valueOf(name);
    }
}
