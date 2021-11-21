package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;
import java.util.function.Supplier;

public class SupplierIterator<T> extends IterInfoNodeAware2Base<T>  {

    private final Supplier<Iterator<T>> from;
    private Iterator<T> it;
    private String name;

    public SupplierIterator(Supplier<Iterator<T>> from, String name) {
        this.from = from;
        this.name = name;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        IterInfoNode a = info("Supplier",
                IterInfoNode.resolveOrNull("base", it, session),
                IterInfoNode.resolveOrNull("supplier", from, session)
        );
        if(name!=null){
            a=a.withName(name);
            a=a.withDescription(name);
        }
        return a;
    }

    @Override
    public boolean hasNext() {
        if (it == null) {
            it = from.get();
        }
        return it.hasNext();
    }

    @Override
    public T next() {
        return it.next();
    }

    @Override
    public String toString() {
        if (name == null) {
            return "supplier(" + from + ")";
        }
        return String.valueOf(name);
    }
}
