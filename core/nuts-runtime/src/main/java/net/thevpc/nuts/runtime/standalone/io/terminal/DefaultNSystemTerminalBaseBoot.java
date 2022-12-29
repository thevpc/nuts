package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NWorkspaceBootOptions;
import net.thevpc.nuts.cmdline.NCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCommandHistory;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.NStreamSystem;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@NComponentScope(NComponentScopeType.PROTOTYPE)
public class DefaultNSystemTerminalBaseBoot extends NSystemTerminalBaseImpl {

    private final Scanner scanner;
    private final NStream out;
    private final NStream err;
    private final InputStream in;
    private final NWorkspace workspace;
    private final NSession session;
    private NLogger LOG;
    private NCommandHistory history;
    private String commandHighlighter;
    private NCommandAutoCompleteResolver commandAutoCompleteResolver;

    public DefaultNSystemTerminalBaseBoot(DefaultNBootModel bootModel) {
        this.session = bootModel.bootSession();
        this.workspace = session.getWorkspace();
        NWorkspaceOptions bo = bootModel.getBootUserOptions();
        NWorkspaceTerminalOptions bootStdFd = new NWorkspaceTerminalOptions(
                bo.getStdin().orElse(System.in),
                bo.getStdout().orElse(System.out),
                bo.getStderr().orElse(System.err),
                "boot"
        );
        NWorkspaceBootOptions bOptions = bootModel.getBootEffectiveOptions();
        NTerminalMode terminalMode = bOptions.getUserOptions().get().getTerminalMode().orElse(NTerminalMode.DEFAULT);
        if (terminalMode == NTerminalMode.DEFAULT) {
            if (bOptions.getUserOptions().get().getBot().orElse(false)) {
                terminalMode = NTerminalMode.FILTERED;
            } else {
                if (bootStdFd.getFlags().contains("ansi")) {
                    terminalMode = NTerminalMode.FORMATTED;
                } else {
                    terminalMode = NTerminalMode.FILTERED;
                }
            }
        }
        this.out = new NStreamSystem(bootStdFd.getOut(), null, null, bootStdFd.getFlags().contains("ansi"),
                bootModel.getBootSession(), this).setTerminalMode(terminalMode);
        this.err = new NStreamSystem(bootStdFd.getErr(), null, null, bootStdFd.getFlags().contains("ansi"),
                bootModel.getBootSession(), this).setTerminalMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
    }


    private NLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = NLogger.of(NSystemTerminalBase.class, session);
        }
        return LOG;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }

    public String readLine(NStream out, NMsg message, NSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
        }
        if (message != null) {
            out.printf("%s", message);
            out.flush();
        }
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(NStream out, NMsg message, NSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
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
    public NStream getOut() {
        return this.out;
    }

    @Override
    public NStream getErr() {
        return this.err;
    }

    @Override
    public NCommandAutoCompleteResolver getAutoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return false;
    }

    @Override
    public NSystemTerminalBase setCommandAutoCompleteResolver(NCommandAutoCompleteResolver autoCompleteResolver) {
        this.commandAutoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NCommandHistory getCommandHistory() {
        return history;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCommandHistory history) {
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


    @Override
    public Object run(NTerminalCommand command, NSession session) {
        return null;
    }

    @Override
    public Cursor getTerminalCursor(NSession session) {
        NSessionUtils.checkSession(session.getWorkspace(), session);
        return (Cursor) run(NTerminalCommand.GET_CURSOR, session);
    }

    @Override
    public Size getTerminalSize(NSession session) {
        NSessionUtils.checkSession(session.getWorkspace(), session);
        return (Size) run(NTerminalCommand.GET_SIZE, session);
    }

    @Override
    public void setStyles(NTextStyles styles, NSession session) {
        String s = NAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            try {
                NWorkspaceTerminalOptions bootStdFd = session.boot().getBootTerminal();
                bootStdFd.getOut().write(s.getBytes());
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
        }
    }
}
