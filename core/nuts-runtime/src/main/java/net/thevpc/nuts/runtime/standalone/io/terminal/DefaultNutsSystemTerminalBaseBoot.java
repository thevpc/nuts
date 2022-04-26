package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsPrintStreamSystem;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@NutsComponentScope(NutsComponentScopeType.PROTOTYPE)
public class DefaultNutsSystemTerminalBaseBoot extends NutsSystemTerminalBaseImpl {

    private final Scanner scanner;
    private final NutsPrintStream out;
    private final NutsPrintStream err;
    private final InputStream in;
    private final NutsWorkspace workspace;
    private final NutsSession session;
    private NutsLogger LOG;
    private NutsCommandHistory history;
    private String commandHighlighter;
    private NutsCommandAutoCompleteResolver commandAutoCompleteResolver;

    public DefaultNutsSystemTerminalBaseBoot(DefaultNutsBootModel bootModel) {
        this.session = bootModel.bootSession();
        this.workspace = session.getWorkspace();
        NutsWorkspaceOptions bo = bootModel.getBootUserOptions();
        NutsBootTerminal bootStdFd = new NutsBootTerminal(
                (bo.getStdin()==null)?System.in:bo.getStdin(),
                (bo.getStdout()==null)?System.out:bo.getStdout(),
                (bo.getStderr()==null)?System.err:bo.getStderr(),
                "boot"
        );
        NutsWorkspaceBootOptions bOptions = bootModel.getBootEffectiveOptions();
        NutsTerminalMode terminalMode = bOptions.getUserOptions().getTerminalMode();
        if (terminalMode == null) {
            if (bOptions.getUserOptions().isBot()) {
                terminalMode = NutsTerminalMode.FILTERED;
            } else {
                if (bootStdFd.getFlags().contains("ansi")) {
                    terminalMode = NutsTerminalMode.FORMATTED;
                } else {
                    terminalMode = NutsTerminalMode.FILTERED;
                }
            }
        }
        this.out = new NutsPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFd.getFlags().contains("ansi"),
                bootModel.getBootSession(), this).setMode(terminalMode);
        this.err = new NutsPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFd.getFlags().contains("ansi"),
                bootModel.getBootSession(), this).setMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
    }


    private NutsLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = NutsLogger.of(NutsSystemTerminalBase.class, session);
        }
        return LOG;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
        }
        if (message != null) {
            out.printf("%s", message);
            out.flush();
        }
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
        }
        if (message != null) {
            out.printf("%s", message);
            out.flush();
        }
        return scanner.nextLine().toCharArray();
    }

    @Override
    public InputStream getIn() {
        return this.in;
    }

    @Override
    public NutsPrintStream getOut() {
        return this.out;
    }

    @Override
    public NutsPrintStream getErr() {
        return this.err;
    }

    @Override
    public NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return false;
    }

    @Override
    public NutsSystemTerminalBase setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver) {
        this.commandAutoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NutsCommandHistory getCommandHistory() {
        return history;
    }

    @Override
    public NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history) {
        this.history = history;
        return this;
    }

    @Override
    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public NutsSystemTerminalBase setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }


    @Override
    public Object run(NutsTerminalCommand command, NutsSession session) {
        return null;
    }

    @Override
    public Cursor getTerminalCursor(NutsSession session) {
        NutsSessionUtils.checkSession(session.getWorkspace(), session);
        return (Cursor) run(NutsTerminalCommand.GET_CURSOR, session);
    }

    @Override
    public Size getTerminalSize(NutsSession session) {
        NutsSessionUtils.checkSession(session.getWorkspace(), session);
        return (Size) run(NutsTerminalCommand.GET_SIZE, session);
    }

    @Override
    public void setStyles(NutsTextStyles styles, NutsSession session) {
        String s = NutsAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            try {
                NutsBootTerminal bootStdFd = session.boot().getBootTerminal();
                bootStdFd.getOut().write(s.getBytes());
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    }
}
