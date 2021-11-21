package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CollectorIterator<T> extends IterInfoNodeAware2Base<T> {

    private String name;
    private Iterator<T> base;
    private List<T> collected = new ArrayList<>();

    public CollectorIterator(String name, Iterator<T> base) {
        this.name = name;
        this.base = base;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("Collector",
                IterInfoNode.resolveOrNull("base",base, session)
        ).withName(name);
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
