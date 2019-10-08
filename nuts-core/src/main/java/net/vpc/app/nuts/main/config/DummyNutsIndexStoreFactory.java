package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.NutsIndexStore;
import net.vpc.app.nuts.NutsIndexStoreFactory;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsSupportLevelContext;

public class DummyNutsIndexStoreFactory implements NutsIndexStoreFactory {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsIndexStore createIndexStore(NutsRepository repository) {
        return new DummyNutsIndexStore(repository);
    }
}
