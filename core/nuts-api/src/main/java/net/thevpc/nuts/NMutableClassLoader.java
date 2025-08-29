package net.thevpc.nuts;

import java.util.List;

public interface NMutableClassLoader {
    ClassLoader asClassLoader();
    List<NDefinition> getLoadedDependencies();
    boolean loadDependencies(NDependency... dependencies);
    boolean isLoadedDependency(NId id);
}
