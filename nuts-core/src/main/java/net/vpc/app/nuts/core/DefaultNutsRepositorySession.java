/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Map;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsSessionTerminal;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositorySession implements NutsRepositorySession {

    private NutsSession session;
    private NutsFetchMode fetchMode;
    private boolean cached;
    private boolean indexed;
    private boolean transitive;
//    private final NutsPropertiesHolder properties = new NutsPropertiesHolder();

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
    public NutsRepositorySession setProperty(String key, Object value) {
        this.session.setProperty(key, value);
        return this;
    }

    @Override
    public NutsRepositorySession setProperties(Map<String, Object> properties) {
        this.session.setProperties(properties);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.session.getProperties();
    }

    @Override
    public Object getProperty(String key) {
        return this.session.getProperty(key);
    }

    @Override
    public NutsRepositorySession copy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return getSession().getTerminal();
    }
    

}
