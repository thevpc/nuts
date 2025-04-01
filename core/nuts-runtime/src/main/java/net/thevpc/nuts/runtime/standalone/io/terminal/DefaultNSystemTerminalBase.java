package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NWorkspaceTerminalOptions;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;

import net.thevpc.nuts.NBootOptions;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamSystem;
import net.thevpc.nuts.runtime.standalone.util.NCachedValue;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NAnsiTermHelper;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.util.Scanner;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNSystemTerminalBase extends NSystemTerminalBaseImpl {
    public static final int THIRTY_SECONDS = 30000;
    NCachedValue<Cursor> termCursor;
    NCachedValue<Size> termSize;

    private Scanner scanner;
    private NTerminalMode outMode = NTerminalMode.FORMATTED;
    private NTerminalMode errMode = NTerminalMode.FORMATTED;
    private NPrintStream out;
    private NPrintStream err;
    private InputStream in;
    private NCmdLineHistory history;
    private String commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;
    private Boolean preferConsole;

    public DefaultNSystemTerminalBase(NWorkspace workspace) {
        super();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        NBootOptions options = NWorkspace.of().getBootOptions();
        NTerminalMode terminalMode = options.getTerminalMode().orElse(NTerminalMode.DEFAULT);
        NWorkspaceTerminalOptions bootStdFd = NWorkspaceExt.of().getModel().bootModel.getBootTerminal();
        if (terminalMode == NTerminalMode.DEFAULT) {
            if (options.getBot().orElse(false) || !bootStdFd.getFlags().contains("ansi")) {
                terminalMode = NTerminalMode.FILTERED;
            } else {
                terminalMode = NTerminalMode.FORMATTED;
            }
        }
        if (bootStdFd.getFlags().contains("tty")) {
            termCursor = new NCachedValue<>(() -> CoreAnsiTermHelper.evalCursor(), THIRTY_SECONDS);
            termSize = new NCachedValue<>(() -> CoreAnsiTermHelper.evalSize(), THIRTY_SECONDS);
        } else {
            termCursor = new NCachedValue<>(() -> null, THIRTY_SECONDS);
            termSize = new NCachedValue<>(() -> null, THIRTY_SECONDS);
        }
        this.out = new NPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFd.getFlags().contains("ansi"),
                 this).setTerminalMode(terminalMode);
        this.err = new NPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFd.getFlags().contains("ansi"),
                 this).setTerminalMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public String readLine(NPrintStream out, NMsg message) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of().stdout();
        }
        if (message != null) {
            out.print(message);
            out.flush();
        }
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg message) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of().stdout();
        }
        if (message != null) {
            out.print(message);
            out.flush();
        }
        if (preferConsole == null) {
            if (NIO.of().isStdin(getIn())) {
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
    public Object run(NTerminalCmd command, NPrintStream printStream) {
        switch (command.getName()) {
            case NTerminalCmd.Ids.GET_CURSOR: {
                return termCursor.getValue();
            }
            case NTerminalCmd.Ids.GET_SIZE: {
                return termSize.getValue();
            }
        }
        String s = NAnsiTermHelper.of().command(command);
        if (s != null) {
            byte[] bytes = s.getBytes();
            printStream.writeRaw(bytes, 0, bytes.length);
            printStream.flush();
        }
        return null;
    }

    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        String s = NAnsiTermHelper.of().styled(styles);
        if (s != null) {
            byte[] bytes = s.getBytes();
            printStream.writeRaw(bytes, 0, bytes.length);
            printStream.flush();
        }
    }

    //    @Override
//    public int getColumns() {
//        int tputCallTimeout = NWorkspace.of().getBootCustomArgument("---nuts.term.tput.call.timeout").getValue().getInt(60);
//        Integer w = NWorkspace.of().getBootCustomArgument("---nuts.term.width").getValue().getInt(null);
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
