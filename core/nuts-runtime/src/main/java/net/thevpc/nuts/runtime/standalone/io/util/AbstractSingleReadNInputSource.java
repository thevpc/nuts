package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NSession;

public abstract class AbstractSingleReadNInputSource extends AbstractNInputSource{
    public AbstractSingleReadNInputSource(NSession session) {
        super(session);
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
