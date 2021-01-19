package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.AbstractNutsIndexStore;

import java.util.Collections;
import java.util.Iterator;

public class DummyNutsIndexStore extends AbstractNutsIndexStore {

    public DummyNutsIndexStore(NutsRepository repository) {
        super(repository);
        super.setEnabled(false);
    }

    @Override
    public Iterator<NutsId> searchVersions(NutsId id, NutsSession session) {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<NutsId> search(NutsIdFilter filter, NutsSession session) {
        return Collections.emptyIterator();
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
