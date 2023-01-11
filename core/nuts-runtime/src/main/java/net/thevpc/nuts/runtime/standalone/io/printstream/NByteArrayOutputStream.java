package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NMemoryOutputStream;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class NByteArrayOutputStream extends NOutputStreamRaw implements NMemoryOutputStream {
    public NByteArrayOutputStream(NSession session) {
        super(new ByteArrayOutputStream(), null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }
    protected NByteArrayOutputStream(ByteArrayOutputStream bos, NSession session) {
        super(bos, null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }

    @Override
    public NOutputStream setSession(NSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NByteArrayOutputStream((ByteArrayOutputStream) out,session);
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
