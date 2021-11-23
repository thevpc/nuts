package net.thevpc.nuts;

import java.util.function.Function;

public interface NutsIterable<T> extends Iterable<T>, NutsDescribable {
    static <T> NutsIterable<T> of(Iterable<T> o, String descr){
        return NutsDescribables.ofIterable(o,e->e.ofString(descr));
    }
    static <T> NutsIterable<T> of(Iterable<T> o,NutsElement descr){
        return NutsDescribables.ofIterable(o,e->descr);
    }
    static <T> NutsIterable<T> of(Iterable<T> o, Function<NutsElements, NutsElement> descr){
        return NutsDescribables.ofIterable(o,descr);
    }
    
    NutsIterator<T> iterator();
}
