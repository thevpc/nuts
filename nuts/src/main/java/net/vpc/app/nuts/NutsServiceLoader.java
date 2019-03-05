package net.vpc.app.nuts;

import java.util.List;

public interface NutsServiceLoader<T extends NutsComponent<B>,B> {

    List<T> loadAll(B criteria);

    T loadBest(B criteria);
}
