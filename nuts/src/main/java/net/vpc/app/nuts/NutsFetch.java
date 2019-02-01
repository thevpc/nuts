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

    NutsId fetchId();

    String fetchContentHash();

    String fetchDescriptorHash();

    NutsDefinition fetchDefinition();

    NutsDescriptor fetchDescriptor();

    String fetchFile();

    NutsFetch setDefaultLocation();
}
