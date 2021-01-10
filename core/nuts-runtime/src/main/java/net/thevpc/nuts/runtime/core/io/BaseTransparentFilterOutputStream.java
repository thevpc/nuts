package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.NutsOutputStreamTransparentAdapter;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public class BaseTransparentFilterOutputStream extends FilterOutputStream
    implements NutsOutputStreamTransparentAdapter
{
    public BaseTransparentFilterOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public OutputStream baseOutputStream() {
        return out;
    }
}
