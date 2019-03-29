/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author vpc
 * @param <T>
 */
public interface NutsQueryBaseOptions<T extends NutsQueryBaseOptions> {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    T setFetchStratery(NutsFetchStrategy mode);

    T setTransitive(boolean transitive);

    T transitive(boolean transitive);

    T transitive();

    /**
     * cache enabled
     *
     * @param cached
     * @return
     */
    

    /**
     * remote only
     *
     * @return
     */
    T remote();

    T local();

    /**
     * installed and local
     *
     * @return
     */
    T offline();

    /**
     * installed, local and remote
     *
     * @return
     */
    T online();

    /**
     * local and remote
     *
     * @return
     */
    T wired();

    /**
     * local and remote
     *
     * @return
     */
    T installed();

    T anyWhere();

    T session(NutsSession session);

    T setSession(NutsSession session);

    T setScope(NutsDependencyScope scope);

    T setScope(NutsDependencyScope... scope);

    T setScope(Collection<NutsDependencyScope> scope);

    T addScope(NutsDependencyScope scope);

    T addScope(Collection<NutsDependencyScope> scope);

    T addScope(NutsDependencyScope... scope);

    T removeScope(Collection<NutsDependencyScope> scope);

    T removeScope(NutsDependencyScope scope);

    T setAcceptOptional(Boolean acceptOptional);

    T setIncludeOptional(boolean includeOptional);

    T setIgnoreCache(boolean ignoreCache);

    T ignoreCache();

    T setIndexed(Boolean indexEnabled);

    T indexed();

    T indexDisabled();

    T includeDependencies();

    T includeDependencies(boolean include);

    T setIncludeDependencies(boolean includeDependencies);

    T setIncludeEffective(boolean includeEffectiveDescriptor);
    
    T setCached(boolean cached);

    T setIncludeFile(boolean includeFile);

    T setIncludeInstallInformation(boolean includeInstallInformation);

    T copyFrom(NutsQueryBaseOptions other);

    T setLocation(String fileOrFolder);

    T setDefaultLocation();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

    String getLocation();

    NutsFetchStrategy getFetchStrategy();

    Boolean getIndexEnabled();

    boolean isIndexed();

    Set<NutsDependencyScope> getScope();

    Boolean getAcceptOptional();

    NutsSession getSession();

    boolean isIgnoreCache();

    boolean isIncludeFile();

    boolean isIncludeInstallInformation();

    boolean isIncludeEffective();

    boolean isIncludeDependencies();

    boolean isTransitive();

    boolean isCached();
}
