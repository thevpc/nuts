package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;

import java.io.IOException;
import java.io.OutputStream;

public class NStreamNull extends NStreamBase {

    private OutputStream nullOS = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            //
        }
    };

    public NStreamNull(NSession session) {
        super(false, NTerminalMode.INHERITED, session, new Bindings(), null);
        getOutputMetaData().setMessage(
                NMsg.ofStyled("<null-stream>", NTextStyle.path()));
    }

    @Override
    public NStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NStreamNull(session);
    }

    @Override
    protected NStream convertImpl(NTerminalMode other) {
        return this;
    }

    @Override
    public NStream flush() {
        return this;
    }

    @Override
    public NStream close() {
        return this;
    }

    @Override
    public NStream write(int b) {
        return this;
    }

    @Override
    public NStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NStream write(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NStream print(String s) {
        return this;
    }

    @Override
    public NStream run(NTerminalCommand command, NSession session) {
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return nullOS;
    }
}
