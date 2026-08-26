package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.io.NOutputStreamBuilder;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputStreamExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

import java.io.OutputStream;

public class DefaultNOutputStreamBuilder implements NOutputStreamBuilder {
    private OutputStream base;
    private NContentMetadata metadata;
    private boolean closeBase = true;
    private Runnable closeAction;

    public DefaultNOutputStreamBuilder() {
    }

    @Override
    public OutputStream base() {
        return base;
    }

    @Override
    public NOutputStreamBuilder base(OutputStream base) {
        this.base = base;
        return this;
    }

    @Override
    public NContentMetadata metadata() {
        return metadata;
    }

    @Override
    public NOutputStreamBuilder metadata(NContentMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean isCloseBase() {
        return closeBase;
    }

    @Override
    public NOutputStreamBuilder closeBase(boolean closeBase) {
        this.closeBase = closeBase;
        return this;
    }

    @Override
    public Runnable closeAction() {
        return closeAction;
    }

    @Override
    public NOutputStreamBuilder closeAction(Runnable closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    @Override
    public OutputStream createOutputStream() {
        if(base==null){
            return NWorkspaceExt.of().getModel().bootModel.nullOutputStream();
        }
        OutputStreamExt o = new OutputStreamExt(
                base, metadata, closeBase, closeAction
        );
        return o;
    }


}
