package net.vpc.app.nuts;

public class NutsContentEvent {
    private NutsId id;
    private NutsDescriptor descriptor;
    private String file;
    private NutsWorkspace workspace;
    private NutsRepository repository;

    public NutsContentEvent(NutsId id, NutsDescriptor descriptor, String file, NutsWorkspace workspace, NutsRepository repository) {
        this.id = id;
        this.descriptor = descriptor;
        this.file = file;
        this.workspace = workspace;
        this.repository = repository;
    }

    public NutsId getId() {
        return id;
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    public String getFile() {
        return file;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsRepository getRepository() {
        return repository;
    }
}
