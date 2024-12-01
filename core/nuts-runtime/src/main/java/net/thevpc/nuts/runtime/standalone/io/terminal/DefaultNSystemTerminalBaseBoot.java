package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NWorkspaceTerminalOptions;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamSystem;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NMsg;

import java.io.InputStream;
import java.util.Scanner;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNSystemTerminalBaseBoot extends NSystemTerminalBaseImpl {

    private final Scanner scanner;
    private final NPrintStream out;
    private final NPrintStream err;
    private final InputStream in;
    private NCmdLineHistory history;
    private String commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;

    public DefaultNSystemTerminalBaseBoot(DefaultNBootModel bootModel) {
        super(bootModel.getWorkspace());
        NBootOptions bo = bootModel.getBootUserOptions();
        NWorkspaceTerminalOptions bootStdFd = new NWorkspaceTerminalOptions(
                bo.getStdin().orElse(System.in),
                bo.getStdout().orElse(System.out),
                bo.getStderr().orElse(System.err),
                bootModel.getBootTerminal().getFlags().toArray(new String[0])
        );
        NTerminalMode terminalMode = bootModel.getBootUserOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT);
        boolean bootStdFdAnsi = bootStdFd.getFlags().contains("ansi");
        if (terminalMode == NTerminalMode.DEFAULT) {
            if (bootModel.getBootUserOptions().getBot().orElse(false)) {
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
                bootModel.getWorkspace(), this).setTerminalMode(terminalMode);
        this.err = new NPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFdAnsi,
                bootModel.getWorkspace(), this).setTerminalMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
    }



    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

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
    public Object run(NTerminalCmd command, NPrintStream printStream) {
        return null;
    }

    @Override
    public Cursor getTerminalCursor() {
        return (Cursor) run(NTerminalCmd.GET_CURSOR, getOut());
    }

    @Override
    public Size getTerminalSize() {
        return (Size) run(NTerminalCmd.GET_SIZE, getOut());
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        String s = NAnsiTermHelper.of(getWorkspace()).styled(styles);
        if (s != null) {
            //try {
                byte[] bytes = s.getBytes();
                printStream.writeRaw(bytes,0,bytes.length);
//                NWorkspaceTerminalOptions bootStdFd = NEnvs.of(session).getBootTerminal();
//                bootStdFd.getOut().write(bytes);
            //} catch (IOException e) {
            //    throw new NIOException(session, e);
            //}
        }
    }
}
