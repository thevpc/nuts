package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspaceCommand;

public abstract class NWorkspaceCommandBaseRepo<T extends NWorkspaceCommand> extends NWorkspaceCommandBase<T> {
    public NWorkspaceCommandBaseRepo(NSession session, String commandName) {
        super(session, commandName);
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
