package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.io.OutputStream;

public class NPrintStreamFormatted extends NPrintStreamRendered {
    public NPrintStreamFormatted(NPrintStreamBase base, NWorkspace workspace, Bindings bindings) {
        super(base, workspace, NTerminalMode.FORMATTED, bindings);
        getMetaData().setMessage(NMsg.ofStyled("<formatted-stream>", NTextStyle.path()));
    }


    @Override
    protected NPrintStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NPrintStreamFiltered(base, workspace, bindings);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public NPrintStream run(NTerminalCmd command) {
        flush();
        print(NText.ofCommand(command));
        flush();
        return this;
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
