package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NAnsiTermHelper;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.spi.NSystemTerminalBaseImpl;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyles;

import java.io.InputStream;

public class AnsiNPrintStreamTerminalBase extends NSystemTerminalBaseImpl {
    private NPrintStream out;
    private NCmdLineHistory history;
    private String commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;

    public AnsiNPrintStreamTerminalBase(NWorkspace workspace,NPrintStream out) {
        super(workspace);
        this.out = out;
    }

    @Override
    public String readLine(NPrintStream out, NMsg message) {
        return null;
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg message) {
        return new char[0];
    }

    @Override
    public InputStream getIn() {
        return null;
    }

    @Override
    public NPrintStream getOut() {
        return out;
    }

    @Override
    public NPrintStream getErr() {
        return null;
    }

    @Override
    public NSystemTerminalBase setCommandAutoCompleteResolver(NCmdLineAutoCompleteResolver autoCompleteResolver) {
        this.commandAutoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NCmdLineHistory getCommandHistory() {
        return history;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCmdLineHistory history) {
        this.history = history;
        return this;
    }

    @Override
    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public NSystemTerminalBase setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    public NCmdLineAutoCompleteResolver getAutoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public Object run(NTerminalCmd command, NPrintStream printStream) {
        String s = NAnsiTermHelper.of(getWorkspace()).command(command);
        if (s != null) {
            byte[] bytes = s.getBytes();
            out.write(bytes, 0, bytes.length);
            out.flush();
        }
        return null;
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        String s = NAnsiTermHelper.of(getWorkspace()).styled(styles);
        if (s != null) {
            byte[] bytes = s.getBytes();
            out.write(bytes, 0, bytes.length);
            out.flush();
        }
    }
}
