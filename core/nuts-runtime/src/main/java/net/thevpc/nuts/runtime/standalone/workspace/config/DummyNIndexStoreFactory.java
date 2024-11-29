package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.env.NIndexStore;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.NRepository;
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
