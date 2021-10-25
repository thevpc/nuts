package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsMemoryPrintStream;
import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;

import java.io.ByteArrayOutputStream;

public class NutsByteArrayPrintStream extends NutsPrintStreamRaw implements NutsMemoryPrintStream {
    public NutsByteArrayPrintStream(NutsSession session) {
        super(new ByteArrayOutputStream(), null, null, session, new Bindings());
    }
    protected NutsByteArrayPrintStream(ByteArrayOutputStream bos,NutsSession session) {
        super(bos, null, null, session, new Bindings());
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NutsByteArrayPrintStream((ByteArrayOutputStream) out,session);
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
