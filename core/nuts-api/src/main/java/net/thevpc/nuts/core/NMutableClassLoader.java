package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NId;

import java.util.List;

public interface NMutableClassLoader {
    ClassLoader asClassLoader();
    List<NDefinition> getLoadedDependencies();
    boolean loadDependencies(NDependency... dependencies);
    boolean isLoadedDependency(NId id);
}
