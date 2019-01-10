package net.vpc.app.nuts;

public interface NutsBootContext {

    String getHome();

    String getWorkspace();

    StoreLocationStrategy getStoreLocationStrategy();

    StoreLocationLayout getStoreLocationLayout();

    String getStoreLocation(StoreFolder folderType);

    NutsId getApiId();

    NutsId getRuntimeId();

    String getRuntimeDependencies();

    String getRepositories();

    String getJavaCommand();

    String getJavaOptions();
}
