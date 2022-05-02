package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.util.NutsIterator;

public class DummyNutsIndexStore extends AbstractNutsIndexStore {

    public DummyNutsIndexStore(NutsRepository repository) {
        super(repository);
        super.setEnabled(false);
    }

    @Override
    public NutsIterator<NutsId> searchVersions(NutsId id, NutsSession session) {
        return IteratorBuilder.emptyIterator();
    }

    @Override
    public NutsIterator<NutsId> search(NutsIdFilter filter, NutsSession session) {
        return IteratorBuilder.emptyIterator();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public NutsIndexStore setEnabled(boolean enabled) {
        super.setEnabled(false);
        return this;
    }

    @Override
    public NutsIndexStore invalidate(NutsId id, NutsSession session) {
        return this;
    }

    @Override
    public NutsIndexStore revalidate(NutsId id, NutsSession session) {
        return this;
    }

    @Override
    public NutsIndexStore subscribe(NutsSession session) {
        return this;
    }

    @Override
    public NutsIndexStore unsubscribe(NutsSession session) {
        return this;
    }

    @Override
    public boolean isSubscribed(NutsSession session) {
        return false;
    }
}
