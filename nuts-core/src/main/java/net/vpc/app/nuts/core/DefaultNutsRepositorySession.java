/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsWorkspace;

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
    private NutsWorkspace ws;

    public DefaultNutsRepositorySession(NutsWorkspace ws, NutsSession session) {
        this.session = session;
        this.ws = ws;
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
            throw new NutsException(ws, "Unable to copy " + this);
        }
        return t;
    }

}
