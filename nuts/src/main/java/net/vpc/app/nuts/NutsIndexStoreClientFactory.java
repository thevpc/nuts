package net.vpc.app.nuts;

public interface NutsIndexStoreClientFactory extends NutsComponent<NutsWorkspace> {

    NutsIndexStoreClient createNutsIndexStoreClient(NutsRepository repository);
}
