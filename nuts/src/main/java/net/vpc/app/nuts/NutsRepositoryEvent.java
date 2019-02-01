package net.vpc.app.nuts;

public class NutsRepositoryEvent {
    private NutsWorkspace workspace;
    private NutsRepository parent;
    private NutsRepository repository;

    public NutsRepositoryEvent(NutsWorkspace workspace, NutsRepository parent, NutsRepository repository) {
        this.workspace = workspace;
        this.parent = parent;
        this.repository = repository;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsRepository getParent() {
        return parent;
    }

    public NutsRepository getRepository() {
        return repository;
    }
}
