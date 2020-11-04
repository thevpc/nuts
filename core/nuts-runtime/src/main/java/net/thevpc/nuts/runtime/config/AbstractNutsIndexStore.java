package net.thevpc.nuts.runtime.config;

import net.thevpc.nuts.NutsIndexStore;
import net.thevpc.nuts.NutsRepository;

import java.util.*;

public abstract class AbstractNutsIndexStore implements NutsIndexStore {

    private NutsRepository repository;
    private boolean enabled = true;
    private Date inaccessibleDate = null;

    public AbstractNutsIndexStore(NutsRepository repository) {
        this.repository = repository;
    }

    protected void setInaccessible() {
        inaccessibleDate = new Date();
    }

    public boolean isInaccessible() {
        if (inaccessibleDate == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - inaccessibleDate.getTime();
        if (elapsed > 1000 * 60 * 5) {
            inaccessibleDate = null;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public NutsIndexStore setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public NutsIndexStore enabled(boolean enabled) {
        return setEnabled(enabled);
    }

    @Override
    public NutsIndexStore enabled() {
        return enabled(true);
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public void setRepository(NutsRepository repository) {
        this.repository = repository;
    }
}
