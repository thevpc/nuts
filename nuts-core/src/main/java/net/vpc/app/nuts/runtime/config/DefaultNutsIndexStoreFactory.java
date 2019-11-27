package net.vpc.app.nuts.runtime.config;

import net.vpc.app.nuts.NutsIndexStore;
import net.vpc.app.nuts.NutsIndexStoreFactory;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsSupportLevelContext;

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
