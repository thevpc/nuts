package net.thevpc.nuts;

import java.util.Comparator;
import java.util.function.Function;

public interface NutsComparator<T> extends Comparator<T>, NutsDescribable {
    static <T> NutsComparator<T> of(Comparator<T> o,String descr){
        return NutsDescribables.ofComparator(o,e->e.ofString(descr));
    }
    static <T> NutsComparator<T> of(Comparator<T> o,NutsElement descr){
        return NutsDescribables.ofComparator(o,e->descr);
    }
    static <T> NutsComparator<T> of(Comparator<T> o, Function<NutsElements, NutsElement> descr){
        return NutsDescribables.ofComparator(o,descr);
    }
}
