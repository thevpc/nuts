package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextStyled;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;

import java.io.IOException;
import java.io.OutputStream;

public class NutsPrintStreamNull extends NutsPrintStreamBase {

    private OutputStream nullOS = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            //
        }
    };

    public NutsPrintStreamNull(NutsSession session) {
        super(false, NutsTerminalMode.INHERITED, session, new Bindings(), null);
        getOutputMetaData().setMessage(
                NutsMessage.ofStyled("<null-stream>", NutsTextStyle.path()));
    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NutsPrintStreamNull(session);
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        return this;
    }

    @Override
    public NutsPrintStream flush() {
        return this;
    }

    @Override
    public NutsPrintStream close() {
        return this;
    }

    @Override
    public NutsPrintStream write(int b) {
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] buf, int off, int len) {
        return this;
    }

    @Override
    public NutsPrintStream write(char[] buf, int off, int len) {
        return this;
    }

    @Override
    public NutsPrintStream print(String s) {
        return this;
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command, NutsSession session) {
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return nullOS;
    }
}
