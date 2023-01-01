package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;

import java.io.IOException;
import java.io.OutputStream;

public class NOutStreamNull extends NOutStreamBase {

    private OutputStream nullOS = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            //
        }
    };

    public NOutStreamNull(NSession session) {
        super(false, NTerminalMode.INHERITED, session, new Bindings(), null);
        getOutputMetaData().setMessage(
                NMsg.ofStyled("<null-stream>", NTextStyle.path()));
    }

    @Override
    public NOutStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NOutStreamNull(session);
    }

    @Override
    protected NOutStream convertImpl(NTerminalMode other) {
        return this;
    }

    @Override
    public NOutStream flush() {
        return this;
    }

    @Override
    public NOutStream close() {
        return this;
    }

    @Override
    public NOutStream write(int b) {
        return this;
    }

    @Override
    public NOutStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NOutStream write(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NOutStream print(String s) {
        return this;
    }

    @Override
    public NOutStream run(NTerminalCommand command, NSession session) {
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return nullOS;
    }
}
