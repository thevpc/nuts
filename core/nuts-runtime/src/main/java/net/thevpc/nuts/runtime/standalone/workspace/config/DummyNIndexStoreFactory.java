package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.core.NIndexStore;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DummyNIndexStoreFactory implements NIndexStoreFactory {

    @Override
    public NIndexStore createIndexStore(NRepository repository) {
        return new DummyNIndexStore(repository);
    }
}
