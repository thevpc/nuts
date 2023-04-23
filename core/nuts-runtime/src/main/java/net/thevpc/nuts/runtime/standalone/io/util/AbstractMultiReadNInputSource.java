package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NSession;

public abstract class AbstractMultiReadNInputSource extends AbstractNInputSource{
    public AbstractMultiReadNInputSource(NSession session) {
        super(session);
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }
}
