package net.vpc.app.nuts;

import java.nio.file.Path;

public class NutsContentEvent {

    private final NutsRepositoryDeploymentOptions deployment;
    /**
     * stored deployment Path, this is Repository dependent
     */
    private final Path path;
    private final NutsWorkspace workspace;
    private final NutsRepository repository;

    public NutsContentEvent(Path path, NutsRepositoryDeploymentOptions deployment, NutsWorkspace workspace, NutsRepository repository) {
        this.path = path;
        this.deployment = deployment;
        this.workspace = workspace;
        this.repository = repository;
    }

    public Path getPath() {
        return path;
    }

    public NutsRepositoryDeploymentOptions getDeployment() {
        return deployment;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsRepository getRepository() {
        return repository;
    }
}
