package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NComparator;

import java.util.Comparator;
import java.util.function.Supplier;

public class NComparatorFromJavaComparator<T> implements NComparator<T> {
    private Comparator<T> comparator;
    private Supplier<NElement> desc;

    public NComparatorFromJavaComparator(Comparator<T> comparator,Supplier<NElement> desc) {
        this.comparator = comparator;
        this.desc = desc;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

    @Override
    public NElement describe() {
        if(desc==null){
            return  NDescribables.ofLateToString(this).get();
        }
        NElement d = desc.get();
        if(d==null){
            return  NDescribables.ofLateToString(this).get();
        }
        return d;
    }

    @Override
    public NComparator<T> withDescription(Supplier<NElement> description) {
        return new NComparatorFromJavaComparator<>(comparator,description);
    }
}
