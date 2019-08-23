/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositorySession implements NutsRepositorySession, Cloneable {

    private NutsSession session;
    private NutsFetchMode fetchMode;
    private boolean cached;
    private boolean indexed;
    private boolean transitive;
    private NutsRepository repository;

    public DefaultNutsRepositorySession(NutsRepository repository,NutsSession session) {
        this.session = session;
        this.repository = repository;
    }

    @Override
    public NutsRepository getRepository() {
        return repository;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsRepositorySession setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsFetchMode getFetchMode() {
        return fetchMode;
    }

    @Override
    public NutsRepositorySession setFetchMode(NutsFetchMode fetchMode) {
        this.fetchMode = fetchMode;
        return this;
    }

    @Override
    public boolean isCached() {
        return cached;
    }

    @Override
    public NutsRepositorySession setCached(boolean enabledCache) {
        this.cached = enabledCache;
        return this;
    }

    @Override
    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public NutsRepositorySession setIndexed(boolean enabledCache) {
        this.indexed = enabledCache;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsRepositorySession setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsRepositorySession copy() {
        NutsRepositorySession t;
        try {
            t = (NutsRepositorySession) clone();
        } catch (CloneNotSupportedException ex) {
            throw new NutsException(getSession().getWorkspace(), "Unable to copy " + this);
        }
        return t;
    }

}
