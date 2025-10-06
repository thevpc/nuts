package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.core.NWorkspace;

public abstract class AbstractSingleReadNInputSource extends AbstractNInputSource{
    public AbstractSingleReadNInputSource(NWorkspace workspace) {
        super();
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
    public long contentLength() {
        return -1;
    }
}
