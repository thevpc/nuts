package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NMemoryStream;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class NByteArrayStream extends NStreamRaw implements NMemoryStream {
    public NByteArrayStream(NSession session) {
        super(new ByteArrayOutputStream(), null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }
    protected NByteArrayStream(ByteArrayOutputStream bos, NSession session) {
        super(bos, null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }

    @Override
    public NStream setSession(NSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NByteArrayStream((ByteArrayOutputStream) out,session);
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
