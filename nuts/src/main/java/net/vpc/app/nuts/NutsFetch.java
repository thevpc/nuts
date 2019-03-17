package net.vpc.app.nuts;

public interface NutsFetch {

    NutsFetch setId(String id);

    NutsFetch setId(NutsId id);

    NutsFetch includeDependencies();

    NutsFetch includeDependencies(boolean include);

    NutsFetch setSession(NutsSession session);

    NutsFetch addScope(NutsDependencyScope[] scopes);

    NutsFetch setAcceptOptional(Boolean acceptOptional);

    NutsFetch setIncludeOptional(boolean includeOptional);

    NutsFetch setIgnoreCache(boolean ignoreCache);

    NutsFetch setIncludeDependencies(boolean includeDependencies);

    NutsFetch setIncludeEffective(boolean includeEffectiveDescriptor);

    NutsFetch setIncludeFile(boolean includeFile);

    NutsFetch setIncludeInstallInformation(boolean includeInstallInformation);

    NutsFetch setIgnoreCache();

    NutsFetch setLocation(String fileOrFolder);

    boolean isIncludeDependencies();

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

    NutsFetch setDefaultLocation();

    boolean isPreferInstalled();

    boolean isInstalledOnly();

    NutsFetch setInstalledOnly(boolean preferInstalled);

    NutsFetch setPreferInstalled(boolean preferInstalled);
}
