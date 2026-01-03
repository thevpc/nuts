package net.thevpc.nuts.runtime.standalone.io.util;

public abstract class AbstractSingleReadNInputSource extends AbstractNInputSource{
    public AbstractSingleReadNInputSource() {
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
    public long getContentLength() {
        return -1;
    }
}
