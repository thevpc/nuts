package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NutsCommandHistory;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsSystemTerminal;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsSystemTerminalBaseImpl;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyles;
import net.thevpc.nuts.text.NutsTexts;

import java.io.InputStream;

public abstract class AbstractSystemTerminalAdapter extends NutsSystemTerminalBaseImpl implements NutsSystemTerminal {

    protected CProgressBar progressBar;
    private NutsWorkspace ws;
    private String commandHighlighter;

    @Override
    public NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        NutsSystemTerminalBase p = getBase();
        if (p != null) {
            return p.getAutoCompleteResolver();
        }
        return null;
    }

    public boolean isAutoCompleteSupported() {
        return getBase().isAutoCompleteSupported();
    }

    @Override
    public NutsSystemTerminalBase setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver) {
        NutsSystemTerminalBase p = getBase();
        if (p != null) {
            p.setCommandAutoCompleteResolver(autoCompleteResolver);
        }
        return this;
    }

    @Override
    public NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history) {
        getBase().setCommandHistory(history);
        return this;
    }

    @Override
    public NutsCommandHistory getCommandHistory() {
        return getBase().getCommandHistory();
    }

    @Override
    public String readLine(NutsMessage message,NutsSession session) {
        NutsSystemTerminalBase p = getBase();
        if (p instanceof NutsSystemTerminal) {
            return ((NutsSystemTerminal) p).readLine(message,session);
        } else {
            return getBase().readLine(out(), message,session);
        }
    }

    @Override
    public char[] readPassword(NutsMessage message,NutsSession session) {
        NutsSystemTerminalBase p = getBase();
        if (p instanceof NutsSystemTerminal) {
            return ((NutsSystemTerminal) p).readPassword(message,session);
        } else {
            return p.readPassword(out(), message,session);
        }
    }



    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NutsPrintStream out() {
        return getOut();
    }

    @Override
    public NutsPrintStream err() {
        return getErr();
    }

//    @Override
//    public boolean isStandardOutputStream(OutputStream out) {
//        if (out == null) {
//            return true;
//        }
//        if (out == System.out || out == CoreIOUtils.out(ws)) {
//            return true;
//        }
//        if (out instanceof NutsOutputStreamTransparentAdapter) {
//            return isStandardOutputStream(((NutsOutputStreamTransparentAdapter) out).baseOutputStream());
//        }
//        return false;
//    }

//    @Override
//    public boolean isStandardErrorStream(OutputStream out) {
//        if (out == null) {
//            return true;
//        }
//        if (out == System.err || out == CoreIOUtils.err(ws)) {
//            return true;
//        }
//        if (out instanceof NutsOutputStreamTransparentAdapter) {
//            return isStandardErrorStream(((NutsOutputStreamTransparentAdapter) out).baseOutputStream());
//        }
//        return false;
//    }
//
//    @Override
//    public boolean isStandardInputStream(InputStream in) {
//        if (in == null) {
//            return true;
//        }
//        if (in == System.in || in == CoreIOUtils.in(ws)) {
//            return true;
//        }
//        if (in instanceof NutsInputStreamTransparentAdapter) {
//            return isStandardInputStream(((NutsInputStreamTransparentAdapter) in).baseInputStream());
//        }
//        return false;
//    }

    @Override
    public NutsSystemTerminal printProgress(float progress, NutsMessage message,NutsSession session) {
        if (session.isProgress()) {
            if (getBase() instanceof NutsSystemTerminal) {
                ((NutsSystemTerminal) getBase()).printProgress(progress, message,session);
            } else {
                getProgressBar(session).printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        NutsTexts.of(session).ofText(message),
                        err()
                );
            }
        }
        return this;
    }
    private CProgressBar getProgressBar(NutsSession session) {
        if (progressBar == null) {
            progressBar = CProgressBar.of(session);
        }
        return progressBar;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        return getBase().readLine(out, message,session);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
        return getBase().readPassword(out, message, session);
    }

    @Override
    public InputStream getIn() {
        return getBase().getIn();
    }

    @Override
    public NutsPrintStream getOut() {
        return getBase().getOut();
    }

    @Override
    public NutsPrintStream getErr() {
        return getBase().getErr();
    }

    public abstract NutsSystemTerminalBase getBase();

    @Override
    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public NutsSystemTerminal setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NutsTerminalCommand command, NutsSession session) {
        return getBase().run(command, session);
    }

    @Override
    public void setStyles(NutsTextStyles styles, NutsSession session) {
        getBase().setStyles(styles, session);
    }
}
