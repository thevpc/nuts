package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsIndexStoreClient;
import net.vpc.app.nuts.NutsIndexStoreClientFactory;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspace;

public class DefaultNutsIndexStoreClientFactory implements NutsIndexStoreClientFactory {
    @Override
    public boolean subscribe(NutsRepository repository) {
        return false;
    }

    @Override
    public void unsubscribe(NutsRepository repository) {

    }

    @Override
    public NutsIndexStoreClient createNutsIndexStoreClient(NutsRepository repository) {
        return null;
    }

    @Override
    public int getSupportLevel(NutsWorkspace criteria) {
        return 1;
    }
}
