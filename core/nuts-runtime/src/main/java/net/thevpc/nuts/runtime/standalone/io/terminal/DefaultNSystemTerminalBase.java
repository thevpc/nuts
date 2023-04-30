package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamSystem;
import net.thevpc.nuts.runtime.standalone.util.NCachedValue;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NLog;

import java.io.*;
import java.util.Scanner;

@NComponentScope(NComponentScopeType.PROTOTYPE)
public class DefaultNSystemTerminalBase extends NSystemTerminalBaseImpl {
    public static OutputStream TERM;
    static {
        try {
            TERM=new FileOutputStream("/home/vpc/vpc-term.out",true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static final int THIRTY_SECONDS = 30000;
    NCachedValue<Cursor> termCursor;
    NCachedValue<Size> termSize;

    private NLog LOG;
    private Scanner scanner;
    private NTerminalMode outMode = NTerminalMode.FORMATTED;
    private NTerminalMode errMode = NTerminalMode.FORMATTED;
    private NPrintStream out;
    private NPrintStream err;
    private InputStream in;
    private NWorkspace workspace;
    private NSession session;
    private NCmdLineHistory history;
    private String commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;
    private Boolean preferConsole;

    public DefaultNSystemTerminalBase() {

    }

    private NLog _LOG() {
        if (LOG == null && session != null) {
            LOG = NLog.of(NSystemTerminalBase.class, session);
        }
        return LOG;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        this.session = criteria.getSession();
        this.workspace = session.getWorkspace();
        NWorkspaceOptions options = NBootManager.of(session).getBootOptions();
        NTerminalMode terminalMode = options.getTerminalMode().orElse(NTerminalMode.DEFAULT);
        NWorkspaceTerminalOptions bootStdFd = NWorkspaceExt.of(session).getModel().bootModel.getBootTerminal();
        if (terminalMode == NTerminalMode.DEFAULT) {
            if (options.getBot().orElse(false) || !bootStdFd.getFlags().contains("ansi")) {
                terminalMode = NTerminalMode.FILTERED;
            } else {
                terminalMode = NTerminalMode.FORMATTED;
            }
        }
        if (bootStdFd.getFlags().contains("tty")) {
            termCursor = new NCachedValue<>(CoreAnsiTermHelper::evalCursor, THIRTY_SECONDS);
            termSize = new NCachedValue<>(CoreAnsiTermHelper::evalSize, THIRTY_SECONDS);
        } else {
            termCursor = new NCachedValue<>(session -> null, THIRTY_SECONDS);
            termSize = new NCachedValue<>(session -> null, THIRTY_SECONDS);
        }
        this.out = new NPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFd.getFlags().contains("ansi"),
                session, this).setTerminalMode(terminalMode);
        this.err = new NPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFd.getFlags().contains("ansi"),
                session, this).setTerminalMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
        return DEFAULT_SUPPORT;
    }

    @Override
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
        if (preferConsole == null) {
            if (NIO.of(session).isStdin(getIn())) {
                Console c = System.console();
                if (c != null) {
                    preferConsole = true;
                }
            }
            if (preferConsole == null) {
                preferConsole = false;
            }
        }
        if (preferConsole) {
            return System.console().readPassword();
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
    public DefaultNSystemTerminalBase setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NTerminalCommand command, NPrintStream printStream, NSession session) {
        switch (command.getName()) {
            case NTerminalCommand.Ids.GET_CURSOR: {
                return termCursor.getValue(session);
            }
            case NTerminalCommand.Ids.GET_SIZE: {
                return termSize.getValue(session);
            }
        }
        String s = NAnsiTermHelper.of(session).command(command, session);
        if (s != null) {
            try {
                byte[] bytes = s.getBytes();
                printStream.writeRaw(bytes,0,bytes.length);
                printStream.flush();
                //NWorkspaceTerminalOptions bootStdFd = NBootManager.of(session).getBootTerminal();
                //PrintStream out2 = bootStdFd.getOut();
                //out2.write(bytes);
                //out2.flush();

                TERM.write(bytes);
                TERM.flush();
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
        }
        return null;
    }

    public void setStyles(NTextStyles styles, NPrintStream printStream, NSession session) {
        String s = NAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            try {
                byte[] bytes = s.getBytes();
                printStream.writeRaw(bytes,0,bytes.length);
                printStream.flush();
//                NWorkspaceTerminalOptions bootStdFd = NBootManager.of(session).getBootTerminal();
//                PrintStream out2 = bootStdFd.getOut();
//                out2.write(s.getBytes());
//                out2.flush();
                TERM.write(s.getBytes());
                TERM.flush();
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
        }
    }

    //    @Override
//    public int getColumns() {
//        int tputCallTimeout = NBootManager.of(session).getBootCustomArgument("---nuts.term.tput.call.timeout").getValue().getInt(60);
//        Integer w = NBootManager.of(session).getBootCustomArgument("---nuts.term.width").getValue().getInt(null);
//        if (w == null) {
//            if (tput_cols == null) {
//                tput_cols = new NutsCachedValue<>(new DefaultAnsiEscapeCommand.TputEvaluator(session), tputCallTimeout);
//            }
//            Integer v = tput_cols.getValue();
//            return v == null ? -1 : v;
//        }
//        return -1;
//    }

}
