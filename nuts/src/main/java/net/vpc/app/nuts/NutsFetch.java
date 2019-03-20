package net.vpc.app.nuts;

import java.util.Collection;

public interface NutsFetch {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    NutsFetch setId(String id);

    NutsFetch setId(NutsId id);

    NutsFetch includeDependencies();

    NutsFetch includeDependencies(boolean include);

    NutsFetch setSession(NutsSession session);

    NutsFetch setScope(NutsDependencyScope scope);

    NutsFetch setScope(NutsDependencyScope... scope);

    NutsFetch setScope(Collection<NutsDependencyScope> scope);

    NutsFetch addScope(NutsDependencyScope scope);

    NutsFetch addScope(Collection<NutsDependencyScope> scope);

    NutsFetch addScope(NutsDependencyScope... scope);

    NutsFetch removeScope(Collection<NutsDependencyScope> scope);

    NutsFetch removeScope(NutsDependencyScope scope);

    NutsFetch setAcceptOptional(Boolean acceptOptional);

    NutsFetch setIncludeOptional(boolean includeOptional);

    NutsFetch setIgnoreCache(boolean ignoreCache);

    NutsFetch ignoreCache();

    NutsFetch setIncludeDependencies(boolean includeDependencies);

    NutsFetch setIncludeEffective(boolean includeEffectiveDescriptor);

    NutsFetch setIncludeFile(boolean includeFile);

    NutsFetch setIncludeInstallInformation(boolean includeInstallInformation);

    NutsFetch setInstalledOnly(boolean preferInstalled);

    NutsFetch setPreferInstalled(boolean preferInstalled);

    NutsFetch setLocation(String fileOrFolder);

    NutsFetch setDefaultLocation();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    /**
     *
     * @return
     */
    boolean isIncludeDependencies();

    boolean isPreferInstalled();

    boolean isInstalledOnly();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsContent fetchContent();

    NutsContent fetchContentOrNull();

    NutsId fetchId();

    NutsId fetchIdOrNull();

    String fetchContentHash();

    String fetchDescriptorHash();

    NutsDefinition fetchDefinition();

    NutsDefinition fetchDefinitionOrNull();

    NutsDescriptor fetchDescriptor();

    NutsDescriptor fetchDescriptorOrNull();

    String fetchFile();

    String fetchFileOrNull();
}
