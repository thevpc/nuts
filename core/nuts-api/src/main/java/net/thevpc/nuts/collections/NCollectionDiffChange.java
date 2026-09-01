package net.thevpc.nuts.collections;

import net.thevpc.nuts.util.NDiffMode;

import java.util.function.Function;

public interface NCollectionDiffChange<T> {
    <H> NCollectionDiffChange<H> map(Function<T, H> f);

    NDiffMode mode();

    T oldValue();

    T newValue();
}
