package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.lib.common.iter.IteratorBuilder;
import net.thevpc.nuts.util.NIterator;

public class DummyNIndexStore extends AbstractNIndexStore {

    public DummyNIndexStore(NRepository repository) {
        super(repository);
        super.setEnabled(false);
    }

    @Override
    public NIterator<NId> searchVersions(NId id, NSession session) {
        return IteratorBuilder.emptyIterator();
    }

    @Override
    public NIterator<NId> search(NIdFilter filter, NSession session) {
        return IteratorBuilder.emptyIterator();
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
    public NIndexStore invalidate(NId id, NSession session) {
        return this;
    }

    @Override
    public NIndexStore revalidate(NId id, NSession session) {
        return this;
    }

    @Override
    public NIndexStore subscribe(NSession session) {
        return this;
    }

    @Override
    public NIndexStore unsubscribe(NSession session) {
        return this;
    }

    @Override
    public boolean isSubscribed(NSession session) {
        return false;
    }
}
