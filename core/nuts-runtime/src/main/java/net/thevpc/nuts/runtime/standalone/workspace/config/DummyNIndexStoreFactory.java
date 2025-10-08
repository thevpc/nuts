package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.core.NIndexStore;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.spi.NScorableContext;

public class DummyNIndexStoreFactory implements NIndexStoreFactory {

    @Override
    public int getScore(NScorableContext criteria) {
        return DEFAULT_SCORE;
    }

    @Override
    public NIndexStore createIndexStore(NRepository repository) {
        return new DummyNIndexStore(repository);
    }
}
