package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.InputStream;
import java.util.Scanner;

@NutsComponentScope(NutsComponentScopeType.PROTOTYPE)
public class DefaultNutsSystemTerminalBase implements NutsSystemTerminalBase {

    private NutsLogger LOG;
    private Scanner scanner;
    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;
    private NutsSession session;
    private NutsCommandHistory history;
    private String commandHighlighter;
    private NutsCommandAutoCompleteResolver commandAutoCompleteResolver;

    public DefaultNutsSystemTerminalBase() {

    }

    private NutsLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = NutsLogger.of(NutsSystemTerminalBase.class, session);
        }
        return LOG;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        this.session = criteria.getSession();
        this.workspace = session.getWorkspace();
        if (workspace != null) {
            NutsWorkspaceOptions options = session.boot().getBootOptions();
            NutsTerminalMode terminalMode = options.getTerminalMode();
            if (terminalMode == null) {
                if (options.isBot()) {
                    terminalMode = NutsTerminalMode.FILTERED;
                } else {
                    terminalMode = NutsTerminalMode.FORMATTED;
                }
            }
            this.out = NutsPrintStreams.of(session).stdout().setMode(terminalMode);
            this.err = NutsPrintStreams.of(session).stderr().setMode(terminalMode);
            this.in = NutsInputStreams.of(session).stdin();
            this.scanner = new Scanner(this.in);
        } else {
            //on uninstall do nothing
        }

        return DEFAULT_SUPPORT;
    }

    @Override
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
    public DefaultNutsSystemTerminalBase setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }
}
