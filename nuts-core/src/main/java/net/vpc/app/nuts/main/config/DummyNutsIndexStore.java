package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.config.AbstractNutsIndexStore;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DummyNutsIndexStore extends AbstractNutsIndexStore {

    public DummyNutsIndexStore(NutsRepository repository) {
        super(repository);
        super.setEnabled(false);
    }

    @Override
    public List<NutsId> searchVersions(NutsId id, NutsRepositorySession session) {
        return Collections.emptyList();
    }

    @Override
    public Iterator<NutsId> search(NutsIdFilter filter, NutsRepositorySession session) {
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
    public NutsIndexStore invalidate(NutsId id) {
        return this;
    }

    @Override
    public NutsIndexStore revalidate(NutsId id) {
        return this;
    }

    @Override
    public NutsIndexStore subscribe() {
        return this;
    }

    @Override
    public NutsIndexStore unsubscribe() {
        return this;
    }

    @Override
    public boolean isSubscribed() {
        return false;
    }
}
