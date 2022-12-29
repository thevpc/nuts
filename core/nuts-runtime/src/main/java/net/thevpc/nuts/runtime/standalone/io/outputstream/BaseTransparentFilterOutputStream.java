package net.thevpc.nuts.runtime.standalone.io.outputstream;

import net.thevpc.nuts.io.NOutputStreamTransparentAdapter;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public class BaseTransparentFilterOutputStream extends FilterOutputStream
    implements NOutputStreamTransparentAdapter
{
    public BaseTransparentFilterOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public OutputStream baseOutputStream() {
        return out;
    }
}
