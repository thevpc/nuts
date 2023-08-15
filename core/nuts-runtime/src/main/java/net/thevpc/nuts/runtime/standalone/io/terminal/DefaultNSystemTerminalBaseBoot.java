package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamSystem;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;

import java.io.InputStream;
import java.util.Scanner;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNSystemTerminalBaseBoot extends NSystemTerminalBaseImpl {

    private final Scanner scanner;
    private final NPrintStream out;
    private final NPrintStream err;
    private final InputStream in;
    private final NWorkspace workspace;
    private final NSession session;
    private NLog LOG;
    private NCmdLineHistory history;
    private String commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;

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
        NBootOptions bOptions = bootModel.getBootEffectiveOptions();
        NTerminalMode terminalMode = bOptions.getUserOptions().get().getTerminalMode().orElse(NTerminalMode.DEFAULT);
        boolean bootStdFdAnsi = bootStdFd.getFlags().contains("ansi");
        if (terminalMode == NTerminalMode.DEFAULT) {
            if (bOptions.getUserOptions().get().getBot().orElse(false)) {
                terminalMode = NTerminalMode.FILTERED;
            } else {
                if (bootStdFdAnsi) {
                    terminalMode = NTerminalMode.FORMATTED;
                } else {
                    terminalMode = NTerminalMode.FILTERED;
                }
            }
        }else if (terminalMode == NTerminalMode.ANSI) {
            terminalMode = NTerminalMode.FORMATTED;
        }
        this.out = new NPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFdAnsi,
                bootModel.getBootSession(), this).setTerminalMode(terminalMode);
        this.err = new NPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFdAnsi,
                bootModel.getBootSession(), this).setTerminalMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
    }


    private NLog _LOG() {
        if (LOG == null && session != null) {
            LOG = NLog.of(NSystemTerminalBase.class, session);
        }
        return LOG;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public String readLine(NPrintStream out, NMsg message, NSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
        }
        if (message != null) {
            out.print(message);
            out.flush();
        }
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg message, NSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
        }
        if (message != null) {
            out.print(message);
            out.flush();
        }
        return scanner.nextLine().toCharArray();
    }

    @Override
    public InputStream getIn() {
        return this.in;
    }

    @Override
    public NPrintStream getOut() {
        return this.out;
    }

    @Override
    public NPrintStream getErr() {
        return this.err;
    }

    @Override
    public NCmdLineAutoCompleteResolver getAutoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return false;
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


    @Override
    public Object run(NTerminalCommand command, NPrintStream printStream, NSession session) {
        return null;
    }

    @Override
    public Cursor getTerminalCursor(NSession session) {
        NSessionUtils.checkSession(session.getWorkspace(), session);
        return (Cursor) run(NTerminalCommand.GET_CURSOR, getOut(), session);
    }

    @Override
    public Size getTerminalSize(NSession session) {
        NSessionUtils.checkSession(session.getWorkspace(), session);
        return (Size) run(NTerminalCommand.GET_SIZE, getOut(), session);
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream, NSession session) {
        String s = NAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            //try {
                byte[] bytes = s.getBytes();
                printStream.writeRaw(bytes,0,bytes.length);
//                NWorkspaceTerminalOptions bootStdFd = NBootManager.of(session).getBootTerminal();
//                bootStdFd.getOut().write(bytes);
            //} catch (IOException e) {
            //    throw new NIOException(session, e);
            //}
        }
    }
}
