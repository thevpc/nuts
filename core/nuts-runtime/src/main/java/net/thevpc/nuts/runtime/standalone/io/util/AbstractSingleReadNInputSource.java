package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NWorkspace;

public abstract class AbstractSingleReadNInputSource extends AbstractNInputSource{
    public AbstractSingleReadNInputSource(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public boolean isMultiRead() {
        return false;
    }

    @Override
    public boolean isKnownContentLength() {
        return false;
    }

    @Override
    public long getContentLength() {
        return -1;
    }
}
