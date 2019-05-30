/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
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
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsFetchCommand extends NutsWorkspaceCommand {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsFetchCommand setId(String id);

    NutsFetchCommand setId(NutsId id);

    NutsFetchCommand nutsApi();

    NutsFetchCommand nutsRuntime();

    NutsFetchCommand id(String id);

    NutsFetchCommand id(NutsId id);

    NutsFetchCommand setLocation(Path fileOrFolder);

    NutsFetchCommand location(Path fileOrFolder);

    NutsFetchCommand setDefaultLocation();

    /**
     * if false, null replaces NutsNotFoundException
     *
     * @param enable
     * @return
     */
    NutsFetchCommand setFailFast(boolean enable);

    NutsFetchCommand failFast(boolean enable);

    NutsFetchCommand failFast();

//    NutsFetch copyFrom(NutsFetch other);
    ////////////////////////////////////////////////////////
    // Getter
    ////////////////////////////////////////////////////////
    NutsId getId();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsContent getResultContent();

    NutsId getResultId();

    String getResultContentHash();

    String getResultDescriptorHash();

    NutsDefinition getResultDefinition();

    NutsDescriptor getResultDescriptor();

    Path getResultPath();

    ///////////////////////
    // REDIFNIED
    ///////////////////////
    NutsFetchCommand copy();

    NutsFetchCommand copyFrom(NutsFetchCommand other);

    ///////////////////////
    // SHARED
    ///////////////////////
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsFetchCommand fetchStratery(NutsFetchStrategy mode);

    NutsFetchCommand setFetchStratery(NutsFetchStrategy mode);

    NutsFetchCommand setTransitive(boolean transitive);

    NutsFetchCommand transitive(boolean transitive);

    NutsFetchCommand transitive();

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
    NutsFetchCommand remote();

    /**
     * installed and local
     *
     * @return
     */
    NutsFetchCommand offline();

    /**
     * installed, local and remote
     *
     * @return
     */
    NutsFetchCommand online();

    /**
     * local and remote
     *
     * @return
     */
    NutsFetchCommand installed();

    NutsFetchCommand anyWhere();

    NutsFetchCommand scope(NutsDependencyScope scope);

    NutsFetchCommand addScope(NutsDependencyScope scope);

    NutsFetchCommand scopes(Collection<NutsDependencyScope> scope);

    NutsFetchCommand addScopes(Collection<NutsDependencyScope> scope);

    NutsFetchCommand scopes(NutsDependencyScope... scope);

    NutsFetchCommand addScopes(NutsDependencyScope... scope);

//    NutsFetchCommand removeScope(Collection<NutsDependencyScope> scope);
    NutsFetchCommand removeScope(NutsDependencyScope scope);

    NutsFetchCommand clearScopes();

    NutsFetchCommand optional();

    NutsFetchCommand optional(Boolean optional);

    NutsFetchCommand setOptional(Boolean optional);

    NutsFetchCommand indexed();

    NutsFetchCommand indexed(Boolean indexEnabled);

    NutsFetchCommand setIndexed(Boolean indexEnabled);

    Boolean getIndexed();

    boolean isIndexed();

    NutsFetchCommand dependencies();

    NutsFetchCommand dependencies(boolean include);

    NutsFetchCommand setDependencies(boolean includeDependencies);

    NutsFetchCommand dependenciesTree();

    NutsFetchCommand dependenciesTree(boolean include);

    NutsFetchCommand setDependenciesTree(boolean includeDependencies);

    NutsFetchCommand setEffective(boolean effective);

    NutsFetchCommand effective(boolean effective);

    NutsFetchCommand effective();

    NutsFetchCommand cached();

    NutsFetchCommand cached(boolean cached);

    NutsFetchCommand setCached(boolean cached);

    NutsFetchCommand content();

    NutsFetchCommand content(boolean includeContent);

    NutsFetchCommand setContent(boolean includeContent);

    NutsFetchCommand installInformation();

    NutsFetchCommand installInformation(boolean includeInstallInformation);

    NutsFetchCommand setInstallInformation(boolean includeInstallInformation);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    Path getLocation();

    NutsFetchStrategy getFetchStrategy();

    Set<NutsDependencyScope> getScope();

    Boolean getOptional();

    boolean isContent();

    boolean isInstallInformation();

    boolean isEffective();

    boolean isDependencies();

    boolean isDependenciesTree();

    boolean isTransitive();

    boolean isCached();

    NutsFetchCommand repositories(Collection<String> value);

    NutsFetchCommand repositories(String... values);

    NutsFetchCommand addRepositories(Collection<String> value);

    NutsFetchCommand removeRepository(String value);

    NutsFetchCommand addRepositories(String... value);

    NutsFetchCommand clearRepositories();

    NutsFetchCommand addRepository(String value);

    NutsFetchCommand repository(String value);

    @Override
    NutsFetchCommand session(NutsSession session);

    @Override
    NutsFetchCommand setSession(NutsSession session);

    @Override
    NutsFetchCommand configure(String... args);

    @Override
    NutsFetchCommand run();
}
