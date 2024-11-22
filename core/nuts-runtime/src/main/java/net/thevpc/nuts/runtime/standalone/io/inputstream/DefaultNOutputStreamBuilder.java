package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.io.NOutputStreamBuilder;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputStreamExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

import java.io.OutputStream;

public class DefaultNOutputStreamBuilder implements NOutputStreamBuilder {
    private NWorkspace workspace;
    private OutputStream base;
    private NContentMetadata metadata;
    private boolean closeBase = true;
    private Runnable closeAction;

    public DefaultNOutputStreamBuilder(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public OutputStream getBase() {
        return base;
    }

    @Override
    public NOutputStreamBuilder setBase(OutputStream base) {
        this.base = base;
        return this;
    }

    @Override
    public NContentMetadata getMetadata() {
        return metadata;
    }

    @Override
    public NOutputStreamBuilder setMetadata(NContentMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean isCloseBase() {
        return closeBase;
    }

    @Override
    public NOutputStreamBuilder setCloseBase(boolean closeBase) {
        this.closeBase = closeBase;
        return this;
    }

    @Override
    public Runnable getCloseAction() {
        return closeAction;
    }

    @Override
    public NOutputStreamBuilder setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    @Override
    public OutputStream createOutputStream() {
        if(base==null){
            return NWorkspaceExt.of().getModel().bootModel.nullOutputStream();
        }
        OutputStreamExt o = new OutputStreamExt(
                base, metadata, closeBase, closeAction, workspace
        );
        return o;
    }


}
