package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NIteratorFromJavaIterator<T> extends NIteratorBase<T> {

    private String name;
    private Iterator<T> base;
    private List<T> collected = new ArrayList<>();

    public NIteratorFromJavaIterator(String name, Iterator<T> base) {
        this.name = name;
        this.base = base;
    }

    @Override
    public NElement describe() {
        return NElements.ofObjectBuilder()
                .name("Collector")
                .addParam(NElements.ofString(name))
                .set("base", NEDesc.describeResolveOrDestruct(base))
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
