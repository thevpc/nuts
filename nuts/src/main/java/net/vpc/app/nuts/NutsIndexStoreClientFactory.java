package net.vpc.app.nuts;

public interface NutsIndexStoreClientFactory extends NutsComponent<NutsWorkspace> {
    boolean subscribe(NutsRepository repository);

    void unsubscribe(NutsRepository repository);

    NutsIndexStoreClient createNutsIndexStoreClient(NutsRepository repository);
}
