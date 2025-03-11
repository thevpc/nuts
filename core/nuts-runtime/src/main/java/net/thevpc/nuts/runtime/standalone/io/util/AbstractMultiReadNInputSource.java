package net.thevpc.nuts.runtime.standalone.io.util;

public abstract class AbstractMultiReadNInputSource extends AbstractNInputSource{
    public AbstractMultiReadNInputSource() {
        super();
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }
}
