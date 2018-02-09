package net.vpc.app.nuts;

@Singleton
public interface NutsDefaultRepositoriesProvider extends NutsComponent<NutsWorkspace>{
    NutsRepositoryDefinition[] getDefaultRepositories();
}
