package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;

import java.io.IOException;
import java.io.OutputStream;

public class NPrintStreamNull extends NPrintStreamBase {

    private OutputStream nullOS = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            //
        }
    };

    public NPrintStreamNull(NSession session) {
        super(false, NTerminalMode.INHERITED, session, new Bindings(), null);
        getMetaData().setMessage(
                NMsg.ofStyled("<null-stream>", NTextStyle.path()));
    }

    @Override
    public NPrintStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NPrintStreamNull(session);
    }

    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        return this;
    }

    @Override
    public NPrintStream flush() {
        return this;
    }

    @Override
    public NPrintStream close() {
        return this;
    }

    @Override
    public NPrintStream write(int b) {
        return this;
    }

    @Override
    public NPrintStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NPrintStream write(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NPrintStream run(NTerminalCommand command, NSession session) {
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return nullOS;
    }

    @Override
    public NPrintStream writeRaw(byte[] buf, int off, int len) {
        return this;
    }
}
