package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

import java.io.File;
import java.nio.file.Path;

import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

public abstract class AbstractNLocks implements NLocks {
    private NWorkspace workspace;
    private Object source;
    private Object resource;

    public AbstractNLocks(NWorkspace workspace) {
        this.workspace = workspace;
    }


    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getResource() {
        return resource;
    }

    @Override
    public NLocks setSource(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NLocks setResource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLocks setResource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLocks setResource(Object source) {
        this.resource=source;
        return this;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

}
