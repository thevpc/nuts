package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSystemTerminal;
import net.thevpc.nuts.runtime.standalone.xtra.time.CProgressBar;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.spi.NSystemTerminalBaseImpl;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NMsg;

import java.io.InputStream;

public abstract class AbstractSystemTerminalAdapter extends NSystemTerminalBaseImpl implements NSystemTerminal {

    protected CProgressBar progressBar;
    private String commandHighlighter;

    public AbstractSystemTerminalAdapter() {
        super();
    }

    @Override
    public NCmdLineAutoCompleteResolver getAutoCompleteResolver() {
        NSystemTerminalBase p = getBase();
        if (p != null) {
            return p.getAutoCompleteResolver();
        }
        return null;
    }

    public boolean isAutoCompleteSupported() {
        return getBase().isAutoCompleteSupported();
    }

    @Override
    public NSystemTerminalBase setCommandAutoCompleteResolver(NCmdLineAutoCompleteResolver autoCompleteResolver) {
        NSystemTerminalBase p = getBase();
        if (p != null) {
            p.setCommandAutoCompleteResolver(autoCompleteResolver);
        }
        return this;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCmdLineHistory history) {
        getBase().setCommandHistory(history);
        return this;
    }

    @Override
    public NCmdLineHistory getCommandHistory() {
        return getBase().getCommandHistory();
    }

    @Override
    public String readLine(NMsg message) {
        NSystemTerminalBase p = getBase();
        if (p instanceof NSystemTerminal) {
            return ((NSystemTerminal) p).readLine(message);
        } else {
            return getBase().readLine(out(), message);
        }
    }

    @Override
    public char[] readPassword(NMsg message) {
        NSystemTerminalBase p = getBase();
        if (p instanceof NSystemTerminal) {
            return ((NSystemTerminal) p).readPassword(message);
        } else {
            return p.readPassword(out(), message);
        }
    }



    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NPrintStream out() {
        return getOut();
    }

    @Override
    public NPrintStream err() {
        return getErr();
    }

    @Override
    public NSystemTerminal printProgress(float progress, NMsg message) {
        NSession session= NSession.of();
        if (session.isProgress()) {
            if (getBase() instanceof NSystemTerminal) {
                ((NSystemTerminal) getBase()).printProgress(progress, message);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        NText.of(message),
                        err()
                );
            }
        }
        return this;
    }
    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CProgressBar.of();
        }
        return progressBar;
    }

    @Override
    public int getScore(NScorableContext criteria) {
        return DEFAULT_SCORE;
    }

    @Override
    public String readLine(NPrintStream out, NMsg message) {
        return getBase().readLine(out, message);
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg message) {
        return getBase().readPassword(out, message);
    }

    @Override
    public InputStream getIn() {
        return getBase().getIn();
    }

    @Override
    public NPrintStream getOut() {
        return getBase().getOut();
    }

    @Override
    public NPrintStream getErr() {
        return getBase().getErr();
    }

    public abstract NSystemTerminalBase getBase();

    @Override
    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public NSystemTerminal setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NTerminalCmd command, NPrintStream printStream) {
        return getBase().run(command, printStream);
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        getBase().setStyles(styles, printStream);
    }
}
