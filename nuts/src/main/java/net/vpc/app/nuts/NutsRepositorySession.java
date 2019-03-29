/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Map;

/**
 *
 * @author vpc
 */
public interface NutsRepositorySession extends NutsTerminalProvider {

    NutsFetchMode getFetchMode();

    NutsSession getSession();

    boolean isCached();

    boolean isTransitive();

    NutsRepositorySession setTransitive(boolean transitive);

    NutsRepositorySession setCached(boolean enabledCache);

    NutsRepositorySession setFetchMode(NutsFetchMode fetchMode);

    NutsRepositorySession setSession(NutsSession session);

    boolean isIndexed();

    NutsRepositorySession setIndexed(boolean enabledCache);

    @Override
    NutsRepositorySession setProperty(String key, Object value);

    @Override
    NutsRepositorySession setProperties(Map<String, Object> properties);

    public NutsRepositorySession copy();
}
