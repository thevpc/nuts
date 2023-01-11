package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;

import java.io.IOException;
import java.io.OutputStream;

public class NOutputStreamNull extends NOutputStreamBase {

    private OutputStream nullOS = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            //
        }
    };

    public NOutputStreamNull(NSession session) {
        super(false, NTerminalMode.INHERITED, session, new Bindings(), null);
        getOutputMetaData().setMessage(
                NMsg.ofStyled("<null-stream>", NTextStyle.path()));
    }

    @Override
    public NOutputStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NOutputStreamNull(session);
    }

    @Override
    protected NOutputStream convertImpl(NTerminalMode other) {
        return this;
    }

    @Override
    public NOutputStream flush() {
        return this;
    }

    @Override
    public NOutputStream close() {
        return this;
    }

    @Override
    public NOutputStream write(int b) {
        return this;
    }

    @Override
    public NOutputStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NOutputStream write(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NOutputStream run(NTerminalCommand command, NSession session) {
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return nullOS;
    }
}
