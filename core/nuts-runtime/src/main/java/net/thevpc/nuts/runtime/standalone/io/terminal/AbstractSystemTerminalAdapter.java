package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCommandHistory;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NSystemTerminal;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.spi.NSystemTerminalBaseImpl;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;

import java.io.InputStream;

public abstract class AbstractSystemTerminalAdapter extends NSystemTerminalBaseImpl implements NSystemTerminal {

    protected CProgressBar progressBar;
    private NWorkspace ws;
    private String commandHighlighter;

    @Override
    public NCommandAutoCompleteResolver getAutoCompleteResolver() {
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
    public NSystemTerminalBase setCommandAutoCompleteResolver(NCommandAutoCompleteResolver autoCompleteResolver) {
        NSystemTerminalBase p = getBase();
        if (p != null) {
            p.setCommandAutoCompleteResolver(autoCompleteResolver);
        }
        return this;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCommandHistory history) {
        getBase().setCommandHistory(history);
        return this;
    }

    @Override
    public NCommandHistory getCommandHistory() {
        return getBase().getCommandHistory();
    }

    @Override
    public String readLine(NMsg message, NSession session) {
        NSystemTerminalBase p = getBase();
        if (p instanceof NSystemTerminal) {
            return ((NSystemTerminal) p).readLine(message,session);
        } else {
            return getBase().readLine(out(), message,session);
        }
    }

    @Override
    public char[] readPassword(NMsg message, NSession session) {
        NSystemTerminalBase p = getBase();
        if (p instanceof NSystemTerminal) {
            return ((NSystemTerminal) p).readPassword(message,session);
        } else {
            return p.readPassword(out(), message,session);
        }
    }



    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NStream out() {
        return getOut();
    }

    @Override
    public NStream err() {
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
    public NSystemTerminal printProgress(float progress, NMsg message, NSession session) {
        if (session.isProgress()) {
            if (getBase() instanceof NSystemTerminal) {
                ((NSystemTerminal) getBase()).printProgress(progress, message,session);
            } else {
                getProgressBar(session).printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        NTexts.of(session).ofText(message),
                        err()
                );
            }
        }
        return this;
    }
    private CProgressBar getProgressBar(NSession session) {
        if (progressBar == null) {
            progressBar = CProgressBar.of(session);
        }
        return progressBar;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String readLine(NStream out, NMsg message, NSession session) {
        return getBase().readLine(out, message,session);
    }

    @Override
    public char[] readPassword(NStream out, NMsg message, NSession session) {
        return getBase().readPassword(out, message, session);
    }

    @Override
    public InputStream getIn() {
        return getBase().getIn();
    }

    @Override
    public NStream getOut() {
        return getBase().getOut();
    }

    @Override
    public NStream getErr() {
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
    public Object run(NTerminalCommand command, NSession session) {
        return getBase().run(command, session);
    }

    @Override
    public void setStyles(NTextStyles styles, NSession session) {
        getBase().setStyles(styles, session);
    }
}
