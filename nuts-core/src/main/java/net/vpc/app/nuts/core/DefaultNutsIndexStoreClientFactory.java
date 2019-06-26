package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsIndexStoreClient;
import net.vpc.app.nuts.NutsIndexStoreClientFactory;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsSupportLevelContext;

public class DefaultNutsIndexStoreClientFactory implements NutsIndexStoreClientFactory {

    @Override
    public NutsIndexStoreClient createIndexStoreClient(NutsRepository repository) {
        return new DefaultNutsIndexStoreClient(repository);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }
}
