package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public interface NutsFetchCommand {

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
     * if true, null replaces NutsNotFoundException
     *
     * @param lenient
     * @return
     */
    NutsFetchCommand setLenient(boolean lenient);

    NutsFetchCommand lenient(boolean lenient);

    NutsFetchCommand lenient();

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

    NutsFetchCommand local();

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
    NutsFetchCommand wired();

    /**
     * local and remote
     *
     * @return
     */
    NutsFetchCommand installed();

    NutsFetchCommand anyWhere();

    NutsFetchCommand session(NutsSession session);

    NutsFetchCommand setSession(NutsSession session);

    NutsFetchCommand scope(NutsDependencyScope scope);

    NutsFetchCommand addScope(NutsDependencyScope scope);

    NutsFetchCommand scopes(Collection<NutsDependencyScope> scope);

    NutsFetchCommand addScopes(Collection<NutsDependencyScope> scope);

    NutsFetchCommand scopes(NutsDependencyScope... scope);

    NutsFetchCommand addScopes(NutsDependencyScope... scope);

//    NutsFetchCommand removeScope(Collection<NutsDependencyScope> scope);
    NutsFetchCommand removeScope(NutsDependencyScope scope);

    NutsFetchCommand clearScopes();

    NutsFetchCommand acceptOptional();

    NutsFetchCommand acceptOptional(Boolean acceptOptional);

    NutsFetchCommand setAcceptOptional(Boolean acceptOptional);

    NutsFetchCommand includeOptional();

    NutsFetchCommand includeOptional(boolean includeOptional);

    NutsFetchCommand setIncludeOptional(boolean includeOptional);

    NutsFetchCommand indexed();

    NutsFetchCommand indexed(Boolean indexEnabled);

    NutsFetchCommand setIndexed(Boolean indexEnabled);

    Boolean getIndexed();

    boolean isIndexed();

    NutsFetchCommand includeDependencies();

    NutsFetchCommand includeDependencies(boolean include);

    NutsFetchCommand setIncludeDependencies(boolean includeDependencies);

    NutsFetchCommand setEffective(boolean effective);

    NutsFetchCommand effective(boolean effective);

    NutsFetchCommand effective();

    NutsFetchCommand cached();

    NutsFetchCommand cached(boolean cached);

    NutsFetchCommand setCached(boolean cached);

    NutsFetchCommand includeContent();

    NutsFetchCommand includeContent(boolean includeContent);

    NutsFetchCommand setIncludeContent(boolean includeContent);

    NutsFetchCommand includeInstallInformation();

    NutsFetchCommand includeInstallInformation(boolean includeInstallInformation);

    NutsFetchCommand setIncludeInstallInformation(boolean includeInstallInformation);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    Path getLocation();

    NutsFetchStrategy getFetchStrategy();

    Set<NutsDependencyScope> getScope();

    Boolean getAcceptOptional();

    NutsSession getSession();

    boolean isIncludeContent();

    boolean isIncludeInstallInformation();

    boolean isEffective();

    boolean isIncludeDependencies();

    boolean isTransitive();

    boolean isCached();

}
