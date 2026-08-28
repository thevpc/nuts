package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NEqualizer;

import java.util.Collection;
import java.util.function.Function;

public interface NCollectionDiffBuilder<T, K> {
    static <T, K> NCollectionDiffBuilder<T,K> of(){
        return NUtilsRPI.of().createCollectionDiffBuilder();
    }

    Collection<T> oldItems();

    NCollectionDiffBuilder<T, K> oldItems(Collection<T> oldItems);

    Collection<T> newItems();

    NCollectionDiffBuilder<T, K> newItems(Collection<T> newItems);

    Function<T, K> idResolver();

    NCollectionDiffBuilder<T, K> idResolver(Function<T, K> idResolver);

    NEqualizer<T> equalizer();

    NCollectionDiffBuilder<T, K> equalizer(NEqualizer<T> equalize);

    NCollectionDiff<T> diff();
}
