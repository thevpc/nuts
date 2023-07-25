package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NIndexStore;
import net.thevpc.nuts.NCallableSupport;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DummyNIndexStoreFactory implements NIndexStoreFactory {

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NCallableSupport.DEFAULT_SUPPORT;
    }

    @Override
    public NIndexStore createIndexStore(NRepository repository) {
        return new DummyNIndexStore(repository);
    }
}
