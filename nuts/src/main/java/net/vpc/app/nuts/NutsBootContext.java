package net.vpc.app.nuts;

public interface NutsBootContext{

    NutsId getApiId();

    NutsId getRuntimeId();

    String getRuntimeDependencies();

    String getRepositories();

    String getJavaCommand();

    String getJavaOptions();
}
