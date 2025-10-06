package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NIndexStore;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DummyNIndexStoreFactory implements NIndexStoreFactory {

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NIndexStore createIndexStore(NRepository repository) {
        return new DummyNIndexStore(repository);
    }
}
