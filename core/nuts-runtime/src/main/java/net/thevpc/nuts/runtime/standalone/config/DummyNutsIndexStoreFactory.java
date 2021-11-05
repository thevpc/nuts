package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsIndexStore;
import net.thevpc.nuts.spi.NutsIndexStoreFactory;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DummyNutsIndexStoreFactory implements NutsIndexStoreFactory {

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsIndexStore createIndexStore(NutsRepository repository) {
        return new DummyNutsIndexStore(repository);
    }
}
