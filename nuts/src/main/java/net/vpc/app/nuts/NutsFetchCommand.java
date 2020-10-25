/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * Fetch command class helps fetching/retrieving a artifact with all of its
 * files.
 *
 * @author vpc
 * @since 0.5.4
 * @category Commands
 */
public interface NutsFetchCommand extends NutsWorkspaceCommand {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    /**
     * set id to fetch to nuts-api (api artifact)
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setNutsApi();

    /**
     * set id to fetch to nuts-core (runtime artifact)
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setNutsRuntime();

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NutsFetchCommand setId(String id);

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NutsFetchCommand setId(NutsId id);

    /**
     * set locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @param fileOrFolder path to store to
     * @return {@code this} instance
     */
    NutsFetchCommand setLocation(Path fileOrFolder);

    /**
     * unset location to store to fetched id and fall back to default location.
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setDefaultLocation();

    /**
     * when true, NutsNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    boolean isFailFast();

    /**
     * set armed (or disarmed) fail safe mode. if true, null replaces
     * NutsNotFoundException.
     *
     * @param enable if true, null replaces NutsNotFoundException.
     * @return {@code this} instance
     */
    NutsFetchCommand setFailFast(boolean enable);

//    NutsFetch copyFrom(NutsFetch other);
    ////////////////////////////////////////////////////////
    // Getter
    ////////////////////////////////////////////////////////
    /**
     * id to fetch
     *
     * @return id to fetch
     */
    NutsId getId();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    /**
     * return result as content
     *
     * @return result as content
     */
    NutsContent getResultContent();

    /**
     * return result as id
     *
     * @return result as id
     */
    NutsId getResultId();

    /**
     * return result as content hash string
     *
     * @return result as content hash string
     */
    String getResultContentHash();

    /**
     * return result as descriptor hash string
     *
     * @return result as descriptor hash string
     */
    String getResultDescriptorHash();

    /**
     * return result as artifact definition
     *
     * @return result as artifact definition
     */
    NutsDefinition getResultDefinition();

    /**
     * return result as descriptor
     *
     * @return result as descriptor
     */
    NutsDescriptor getResultDescriptor();

    /**
     * return result as InstallInformation
     *
     * @return result as InstallInformation
     * @since 0.8.0
     */
    NutsInstallInformation getResultInstallInformation();

    /**
     * return result as content path
     *
     * @return result as content path
     */
    Path getResultPath();

    ///////////////////////
    // REDIFNIED
    ///////////////////////
    /**
     * create copy (new instance) of {@code this} command
     *
     * @return copy (new instance) of {@code this} command
     */
    NutsFetchCommand copy();

    /**
     * copy into {@code this} from {@code other} fetch command
     *
     * @param other copy into {@code this} from {@code other} fetch command
     * @return {@code this} instance
     */
    NutsFetchCommand copyFrom(NutsFetchCommand other);

    ///////////////////////
    // SHARED
    ///////////////////////
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    /**
     * set fetch strategy.
     *
     * @param fetchStrategy fetch strategy
     * @return {@code this} instance
     */
    NutsFetchCommand setFetchStrategy(NutsFetchStrategy fetchStrategy);

    /**
     * set or unset transitive mode
     *
     * @param enable if true, transitive mode is armed
     * @return {@code this} instance
     */
    NutsFetchCommand setTransitive(boolean enable);

    /**
     * remote only
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setRemote();

    /**
     * local only (installed or not)
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setOffline();

    /**
     * local or remote. If local result found will not fetch remote.
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setOnline();

    /**
     * all artifacts (local and remote). If local result found will any way
     * fetch remote.
     *
     * @return {@code this} instance
     */
    NutsFetchCommand setAnyWhere();

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)
     * } and {@link #setDependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsFetchCommand addScope(NutsDependencyScopePattern scope);

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)
     * } and {@link #setDependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsFetchCommand addScope(NutsDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)
     * } and {@link #setDependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsFetchCommand addScopes(NutsDependencyScope... scope);

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)}
     * and {@link #setDependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsFetchCommand addScopes(NutsDependencyScopePattern... scope);

    /**
     * remove dependency scope filter.
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsFetchCommand removeScope(NutsDependencyScope scope);

    /**
     * remove dependency scope filter.
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsFetchCommand removeScope(NutsDependencyScopePattern scope);

    /**
     * remove all dependency scope filters.
     *
     * @return {@code this} instance
     */
    NutsFetchCommand clearScopes();

    /**
     * set option filter. if null filter is removed. if false only non optional
     * will be retrieved. if true, only optional will be retrieved.
     *
     * @param enable option filter
     * @return {@code this} instance
     */
    NutsFetchCommand setOptional(Boolean enable);

    /**
     * set index filter.if null index is removed. if false do not consider index. 
     * if true, consider index.
     *
     * @param enable index filter.
     * @return {@code this} instance
     */
    NutsFetchCommand setIndexed(Boolean enable);

    /**
     * return  {@code getIndexed() == null || getIndexed()}
     * @return  {@code getIndexed() == null || getIndexed()}
     */
    boolean isIndexed();

    /**
     * enable/disable dependencies list retrieval
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsFetchCommand setDependencies(boolean enable);

    /**
     * enable/disable dependencies tree retrieval
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsFetchCommand setDependenciesTree(boolean enable);

    /**
     * enable/disable effective descriptor evaluation
     * @param enable if true evaluation is enabled.
     * @return {@code this} instance
     */
    NutsFetchCommand setEffective(boolean enable);

    /**
     * enable/disable retrieval from cache
     * @param enable if true cache is enabled.
     * @return {@code this} instance
     */
    NutsFetchCommand setCached(boolean enable);

    /**
     * enable/disable retrieval of content info
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsFetchCommand setContent(boolean enable);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    
    /**
     * get locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @return location path
     */
    Path getLocation();

    /**
     * return fetch strategy
     * @return fetch strategy
     */
    NutsFetchStrategy getFetchStrategy();

    /**
     * dependencies scope filters
     * @return dependencies scope filters
     */
    Set<NutsDependencyScope> getScope();

    /**
     * optional filter
     * @return optional filter
     */
    Boolean getOptional();

    /**
     * content filter
     * @return content filter
     */
    boolean isContent();


    /**
     * effective filter
     * @return effective filter
     */
    boolean isEffective();

    /**
     * if true dependencies list is retrieved
     * @return dependencies list retrieval status
     */
    boolean isDependencies();

    /**
     * if true dependencies tree is retrieved
     * @return dependencies tree retrieval status
     */
    boolean isDependenciesTree();

    /**
     * transitive filter
     * @return transitive filter
     */
    boolean isTransitive();

    /**
     * cache filter
     * @return cache filter
     */
    boolean isCached();

    /**
     * add repository filter
     * @param value repository filter
     * @return {@code this} instance
     */
    NutsFetchCommand addRepositories(Collection<String> value);

    /**
     * remove repository filter
     * @param value repository filter
     * @return {@code this} instance
     */
    NutsFetchCommand removeRepository(String value);

    /**
     * add repository filter
     * @param values repository filter
     * @return {@code this} instance
     */
    NutsFetchCommand addRepositories(String... values);

    /**
     * remove all repository filters
     * @return {@code this} instance
     */
    NutsFetchCommand clearRepositories();

    /**
     * add repository filter
     * @param value repository filter
     * @return {@code this} instance
     */
    NutsFetchCommand addRepository(String value);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsFetchCommand copySession();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsFetchCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsFetchCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsFetchCommand run();

    /**
     * search for non installed packages
     * @return {@code this} instance
     */
    NutsFetchCommand installed();

    /**
     * search for non installed packages
     * @return {@code this} instance
     */
    NutsFetchCommand notInstalled();

    /**
     * search for installed/non installed packages
     * @param value new value
     * @return {@code this} instance
     */
    NutsFetchCommand installed(Boolean value);

    /**
     * search for installed/non installed packages
     * @param value new value
     * @return {@code this} instance
     */
    NutsFetchCommand setInstalled(Boolean value);

    /**
     * return installed/non installed packages flag
     * @return {@code this} instance
     */
    Boolean getInstalled();

}
