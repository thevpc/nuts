package net.thevpc.nuts.runtime.config;

import net.thevpc.nuts.NutsIndexStore;
import net.thevpc.nuts.NutsIndexStoreFactory;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsSupportLevelContext;

public class DefaultNutsIndexStoreFactory implements NutsIndexStoreFactory {

    @Override
    public NutsIndexStore createIndexStore(NutsRepository repository) {
        return new DefaultNutsIndexStore(repository);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }
}
