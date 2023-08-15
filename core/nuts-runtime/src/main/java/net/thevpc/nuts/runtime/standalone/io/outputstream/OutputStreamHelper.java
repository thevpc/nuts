package net.thevpc.nuts.runtime.standalone.io.outputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NOutputStreamTransparentAdapter;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class OutputStreamHelper implements OutputHelper {
    private OutputStream rawOutput;
    private OutputStream rawOutput0;
    private PrintStream ps;
    private NSession session;

    public OutputStreamHelper(OutputStream rawOutput, NSession session) {
        this.session = session;
        this.rawOutput = rawOutput;
        this.rawOutput0 = rawOutput;
        int loopGard = 100;
        while (loopGard > 0) {
            if (rawOutput0 instanceof NOutputStreamTransparentAdapter) {
                rawOutput0 = ((NOutputStreamTransparentAdapter) rawOutput0).baseOutputStream();
            } else {
                break;
            }
            loopGard--;
        }
        if (rawOutput0 instanceof NOutputStreamTransparentAdapter) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid rawOutput"));
        }
    }

    @Override
    public void write(byte[] b, int offset, int len) {
        try {
            rawOutput0.write(b, offset, len);
        } catch (IOException ex) {
            throw new NIOException(session,ex);
        }
    }
    //@Override
    public void write(char[] b, int offset, int len) {
        if(ps!=null) {
            if (offset == 0 && len == b.length) {
                ps.print(b);
            } else {
                ps.print(Arrays.copyOfRange(b, offset, len));
            }
        }else {
            String s=new String(b,offset,len);
            byte[] bb = s.getBytes();
            try {
                rawOutput0.write(bb, 0, bb.length);
            } catch (IOException ex) {
                throw new NIOException(session,ex);
            }
        }
    }

    @Override
    public void flush() {
        try {
            rawOutput0.flush();
        } catch (IOException e) {
            //
        }
    }
}
