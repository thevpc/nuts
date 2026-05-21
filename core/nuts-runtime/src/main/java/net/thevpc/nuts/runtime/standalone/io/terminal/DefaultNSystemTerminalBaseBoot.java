package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.core.NBootOptions;
import net.thevpc.nuts.boot.NWorkspaceTerminalOptions;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.io.NonClosableInputStream;
import net.thevpc.nuts.io.NonClosablePrintStream;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamSystem;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.io.NAnsiTermHelper;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.io.InputStream;
import java.util.Scanner;

@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNSystemTerminalBaseBoot extends NSystemTerminalBaseImpl {

    private final Scanner scanner;
    private final NPrintStream out;
    private final NPrintStream err;
    private final InputStream in;
    private NCmdLineHistory history;
    private NTerminalFormatter commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;
    protected boolean lastWasProgress=false;

    public DefaultNSystemTerminalBaseBoot(DefaultNBootModel bootModel) {
        super();
        NBootOptions bo = bootModel.getBootUserOptions();
        NWorkspaceTerminalOptions bootStdFd = new NWorkspaceTerminalOptions(
                new NonClosableInputStream(bo.stdin().orElse(System.in)),
                new NonClosablePrintStream(bo.stdout().orElse(System.out)),
                new NonClosablePrintStream(bo.stderr().orElse(System.err)),
                bootModel.getBootTerminal().getFlags().toArray(new String[0])
        );
        NTerminalMode terminalMode = bootModel.getBootUserOptions().terminalMode().orElse(NTerminalMode.DEFAULT);
        boolean bootStdFdAnsi = bootStdFd.getFlags().contains("ansi");
        if (terminalMode == NTerminalMode.DEFAULT) {
            if (bootModel.getBootUserOptions().bot().orElse(false)) {
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
        if(bootStdFd.getOut()==bootStdFd.getErr()){
            this.out = new NPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFdAnsi,
                    this).terminalMode(terminalMode);
            this.err = this.out;
        }else {
            this.out = new NPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFdAnsi,
                    this).terminalMode(terminalMode);
            this.err = new NPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFdAnsi,
                    this).terminalMode(terminalMode);
        }
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
    }

    public boolean isLastWasProgress() {
        return lastWasProgress;
    }

    public void lastWasProgress(boolean lastWasProgress) {
        this.lastWasProgress = lastWasProgress;
    }


    public String readLine(NPrintStream out, NMsg message) {
        if (out == null) {
            out = out();
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
            out = out();
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
    public InputStream in() {
        return this.in;
    }

    @Override
    public NPrintStream out() {
        return this.out;
    }

    @Override
    public NPrintStream err() {
        return this.err;
    }

    @Override
    public NCmdLineAutoCompleteResolver autoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return false;
    }

    @Override
    public NSystemTerminalBase commandAutoCompleteResolver(NCmdLineAutoCompleteResolver autoCompleteResolver) {
        this.commandAutoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NCmdLineHistory commandHistory() {
        return history;
    }

    @Override
    public NSystemTerminalBase commandHistory(NCmdLineHistory history) {
        this.history = history;
        return this;
    }

    @Override
    public NTerminalFormatter commandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public NSystemTerminalBase commandHighlighter(NTerminalFormatter commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }


    @Override
    public Object run(NTerminalCmd command, NPrintStream printStream) {
        return null;
    }

    @Override
    public Cursor terminalCursor() {
        return (Cursor) run(NTerminalCmd.GET_CURSOR, out());
    }

    @Override
    public Size terminalSize() {
        return (Size) run(NTerminalCmd.GET_SIZE, out());
    }

    @Override
    public void styles(NTextStyles styles, NPrintStream printStream) {
        String s = NAnsiTermHelper.of().styled(styles);
        if (s != null) {
            //try {
                byte[] bytes = s.getBytes();
                printStream.writeRaw(bytes,0,bytes.length);
//                NWorkspaceTerminalOptions bootStdFd = NWorkspace.of().getBootTerminal();
//                bootStdFd.getOut().write(bytes);
            //} catch (IOException e) {
            //    throw new NIOException(session, e);
            //}
        }
    }
}
