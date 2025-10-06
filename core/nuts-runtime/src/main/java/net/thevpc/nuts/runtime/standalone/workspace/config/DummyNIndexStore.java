package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NIndexStore;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIterator;

public class DummyNIndexStore extends AbstractNIndexStore {

    public DummyNIndexStore(NRepository repository) {
        super(repository);
        super.setEnabled(false);
    }

    @Override
    public NIterator<NId> searchVersions(NId id) {
        return NIteratorBuilder.emptyIterator();
    }

    @Override
    public NIterator<NId> search(NDefinitionFilter filter) {
        return NIteratorBuilder.emptyIterator();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public NIndexStore setEnabled(boolean enabled) {
        super.setEnabled(false);
        return this;
    }

    @Override
    public NIndexStore invalidate(NId id) {
        return this;
    }

    @Override
    public NIndexStore revalidate(NId id) {
        return this;
    }

    @Override
    public NIndexStore subscribe() {
        return this;
    }

    @Override
    public NIndexStore unsubscribe() {
        return this;
    }

    @Override
    public boolean isSubscribed() {
        return false;
    }
}
