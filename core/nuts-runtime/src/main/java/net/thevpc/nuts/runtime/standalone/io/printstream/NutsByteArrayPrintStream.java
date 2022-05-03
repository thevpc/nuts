package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsMemoryPrintStream;
import net.thevpc.nuts.io.NutsOutputTargetMetadata;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class NutsByteArrayPrintStream extends NutsPrintStreamRaw implements NutsMemoryPrintStream {
    public NutsByteArrayPrintStream(NutsSession session) {
        super(new ByteArrayOutputStream(), null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NutsMessage.ofNtf(NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path()))
        );
    }
    protected NutsByteArrayPrintStream(ByteArrayOutputStream bos,NutsSession session) {
        super(bos, null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NutsMessage.ofNtf(NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path()))
        );
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

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
