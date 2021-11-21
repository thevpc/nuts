package net.thevpc.nuts.runtime.bundles.iter;

import java.util.Iterator;

public interface IterInfoNodeAware2<T> extends Iterator<T>, IterInfoNodeAware {
    void attachInfo(IterInfoNode nfo);
}
