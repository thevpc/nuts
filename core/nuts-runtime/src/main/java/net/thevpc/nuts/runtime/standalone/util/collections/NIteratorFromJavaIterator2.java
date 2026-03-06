package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIteratorBase;

import java.util.Iterator;
import java.util.function.Consumer;

public class NIteratorFromJavaIterator2<T> extends NIteratorBase<T> {

    private String name;
    private Iterator<T> base;
    private Consumer<T> consumer;

    public NIteratorFromJavaIterator2(String name, Iterator<T> base,Consumer<T> consumer) {
        this.name = name;
        this.base = base;
    }

    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .name("Collector")
                .addParam(NElement.ofString(name))
                .set("base", NDescribables.describeResolveOrDestruct(base))
                .build();
    }

    @Override
    public boolean hasNextImpl() {
        return base.hasNext();
    }

    @Override
    public T next() {
        T x = base.next();
        consumer.accept(x);
        return x;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "collector(" + base + ")";
        }
        return String.valueOf(name);
    }
}
