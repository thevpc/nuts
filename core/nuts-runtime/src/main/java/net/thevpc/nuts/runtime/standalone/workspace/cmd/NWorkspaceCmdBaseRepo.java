package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NWorkspaceCmd;

public abstract class NWorkspaceCmdBaseRepo<T extends NWorkspaceCmd> extends NWorkspaceCmdBase<T> {
    public NWorkspaceCmdBaseRepo(String commandName) {
        super(commandName);
    }

    protected NRepository repository;

    public NRepository getRepository() {
        return repository;
    }

    public T setRepository(NRepository repository) {
        this.repository = repository;
        return (T) this;
    }
}
