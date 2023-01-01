package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutMemoryStream;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class NOutByteArrayStream extends NOutStreamRaw implements NOutMemoryStream {
    public NOutByteArrayStream(NSession session) {
        super(new ByteArrayOutputStream(), null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }
    protected NOutByteArrayStream(ByteArrayOutputStream bos, NSession session) {
        super(bos, null, null, session, new Bindings(),null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }

    @Override
    public NOutStream setSession(NSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NOutByteArrayStream((ByteArrayOutputStream) out,session);
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
