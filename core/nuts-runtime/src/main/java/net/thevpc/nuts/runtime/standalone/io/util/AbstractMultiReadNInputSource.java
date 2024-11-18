package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NWorkspace;

public abstract class AbstractMultiReadNInputSource extends AbstractNInputSource{
    public AbstractMultiReadNInputSource(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }
}
