package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsQuestion;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.InputStream;

public abstract class AbstractSystemTerminalAdapter extends AbstractNutsTerminal implements NutsSystemTerminal{

    protected CProgressBar progressBar;
    private NutsWorkspace ws;

    @Override
    public NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        NutsSystemTerminalBase p = getParent();
        if (p != null) {
            return p.getAutoCompleteResolver();
        }
        return null;
    }

    public boolean isAutoCompleteSupported() {
        return getParent().isAutoCompleteSupported();
    }

    @Override
    public NutsSystemTerminalBase setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver) {
        NutsSystemTerminalBase p = getParent();
        if (p != null) {
            p.setCommandAutoCompleteResolver(autoCompleteResolver);
        }
        return this;
    }

    @Override
    public NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history) {
        getParent().setCommandHistory(history);
        return this;
    }

    @Override
    public NutsCommandHistory getCommandHistory() {
        return getParent().getCommandHistory();
    }

    @Override
    public NutsCommandReadHighlighter getCommandReadHighlighter() {
        return getParent().getCommandReadHighlighter();
    }

    @Override
    public NutsSystemTerminalBase setCommandReadHighlighter(NutsCommandReadHighlighter commandReadHighlighter) {
        getParent().setCommandReadHighlighter(commandReadHighlighter);
        return this;
    }

    @Override
    public String readLine(NutsMessage message,NutsSession session) {
        NutsSystemTerminalBase p = getParent();
        if (p instanceof NutsSystemTerminal) {
            return ((NutsSystemTerminal) p).readLine(message,session);
        } else {
            return getParent().readLine(out(), message,session);
        }
    }

    @Override
    public char[] readPassword(NutsMessage message,NutsSession session) {
        NutsSystemTerminalBase p = getParent();
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
        if (CoreNutsUtils.acceptProgress(session)) {
            if (getParent() instanceof NutsSystemTerminal) {
                ((NutsSystemTerminal) getParent()).printProgress(progress, message,session);
            } else {
                getProgressBar(session).printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        session.getWorkspace().text().toText(message).toString(),
                        err()
                );
            }
        }
        return this;
    }
    private CProgressBar getProgressBar(NutsSession session) {
        if (progressBar == null) {
            progressBar = CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        return getParent().readLine(out, message,session);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
        return getParent().readPassword(out, message, session);
    }

    @Override
    public InputStream getIn() {
        return getParent().getIn();
    }

    @Override
    public NutsPrintStream getOut() {
        return getParent().getOut();
    }

    @Override
    public NutsPrintStream getErr() {
        return getParent().getErr();
    }

    public abstract NutsSystemTerminalBase getParent();



//    @Override
//    public NutsTerminal sendOutCommand(NutsTerminalCommand command) {
//        session.getWorkspace().term().sendTerminalCommand(out(), command);
//        return this;
//    }

//    @Override
//    public NutsTerminal sendErrCommand(NutsTerminalCommand command) {
//        session.getWorkspace().term().sendTerminalCommand(out(), command);
//        return this;
//    }
}
