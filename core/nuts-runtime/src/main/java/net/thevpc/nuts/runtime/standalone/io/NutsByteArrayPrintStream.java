package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsMemoryPrintStream;
import net.thevpc.nuts.NutsSession;

import java.io.ByteArrayOutputStream;

public class NutsByteArrayPrintStream extends NutsPrintStreamRaw implements NutsMemoryPrintStream {
    public NutsByteArrayPrintStream(NutsSession session) {
        super(new ByteArrayOutputStream(), null, null, session, new Bindings());

    }

    @Override
    public byte[] getBytes() {
        flush();
        return ((ByteArrayOutputStream)out).toByteArray();
    }

    @Override
    public String toString() {
        flush();
        return out.toString();
    }
}
