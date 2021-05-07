package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.format.text.FPrint;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;

@NutsPrototype
public class DefaultNutsSystemTerminalBase implements NutsSystemTerminalBase, NutsSessionAware {

    private NutsLogger LOG;
    private Scanner scanner;
    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;
    private NutsSession session;
    private boolean bootSession;
//    protected CProgressBar progressBar;

    public DefaultNutsSystemTerminalBase() {

    }

    public DefaultNutsSystemTerminalBase(boolean bootSession) {
        this.bootSession = bootSession;
    }

    @Override
    public void setSession(NutsSession session) {
        setSession(session, bootSession, session.getWorkspace().config().options());
    }

    public void setSession(NutsSession session, boolean bootSession, NutsWorkspaceOptions options) {
        this.session = session;
        this.workspace = session == null ? null : session.getWorkspace();
        if (workspace != null) {
//        LOG=workspace.log().of(DefaultNutsSystemTerminalBase.class);
            NutsTerminalMode terminalMode = options.getTerminalMode();
            if (terminalMode == null) {
                if (options.isBot()) {
                    terminalMode = NutsTerminalMode.FILTERED;
                } else {
                    terminalMode = NutsTerminalMode.FORMATTED;
                }
            }
            if (bootSession) {
                this.outMode = terminalMode;
                this.errMode = terminalMode;
                this.out = CoreIOUtils.toPrintStream(CoreIOUtils.convertOutputStream(System.out, terminalMode, session), session);
                this.err = CoreIOUtils.toPrintStream(CoreIOUtils.convertOutputStream(System.err, terminalMode, session), session);
                this.in = System.in;
            } else {
                setOutMode(terminalMode);
                setErrMode(terminalMode);
                NutsIOManager ioManager = workspace.io().setSession(session);
                this.out = ioManager.createPrintStream(CoreIOUtils.out(workspace), terminalMode);
                this.err = ioManager.createPrintStream(CoreIOUtils.err(workspace), terminalMode);//.setColor(NutsPrintStream.RED);
                this.in = CoreIOUtils.in(workspace);
            }
            this.scanner = new Scanner(this.in);
        } else {
            //on uninstall do nothing
        }
    }

    private NutsLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = workspace.log().setSession(session).of(NutsSystemTerminalBase.class);
        }
        return LOG;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return outMode;
    }

    @Override
    public NutsSystemTerminalBase setOutMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.FORMATTED;
        }
        if (_LOG() != null) {
            _LOG().with().session(session).level(Level.CONFIG).verb(NutsLogVerb.UPDATE).formatted().log("change terminal Out mode : {0}",
                    workspace.formats().text().forStyled(mode.id(), NutsTextStyle.primary(1))
            );
        }
        FPrint.installStdOut(this.outMode = mode, session);
        return this;
    }

//    private CProgressBar getProgressBar() {
//        if(progressBar==null){
//            progressBar= CoreTerminalUtils.createProgressBar(session);
//        }
//        return progressBar;
//    }
//
//    @Override
//    public NutsTerminalBase printProgress(float progress, String prompt, Object... params) {
//        if(getParent()!=null) {
//            getParent().printProgress(progress, prompt, params);
//        }else{
//            getProgressBar().printProgress(
//                    Float.isNaN(progress)?-1:
//                            (int)(progress*100),
//                    session.getWorkspace().formats().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
//                    getErr()
//            );
//        }
//        return this;
//    }
//
//    @Override
//    public NutsTerminalBase printProgress(String prompt, Object... params) {
//        if(getParent()!=null) {
//            getParent().printProgress(prompt, params);
//        }else{
//            getProgressBar().printProgress(-1,
//                    session.getWorkspace().formats().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
//                    getErr()
//            );
//        }
//        return this;
//    }

    @Override
    public NutsSystemTerminalBase setErrMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.FORMATTED;
        }
        if (_LOG() != null) {
            _LOG().with().session(session).level(Level.CONFIG).verb(NutsLogVerb.UPDATE).formatted().log("change terminal Err mode : {0}",
                    workspace.formats().text().forStyled(mode.id(), NutsTextStyle.primary(1))
            );
        }
        FPrint.installStdErr(this.errMode = mode, session);
        return this;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return errMode;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return DEFAULT_SUPPORT;
    }

//    @Override
    public String readLine(String prompt, Object... params) {
        return readLine(getOut(), prompt, params);
    }

//    @Override
    public char[] readPassword(String prompt, Object... params) {
        return readPassword(getOut(), prompt, params);
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = CoreIOUtils.out(workspace);
        }
        out.printf(prompt, params);
        out.flush();
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = CoreIOUtils.out(workspace);
        }
        out.printf(prompt, params);
        return scanner.nextLine().toCharArray();
    }

    @Override
    public InputStream getIn() {
        return this.in;
    }

    @Override
    public PrintStream getOut() {
        return this.out;
    }

    @Override
    public PrintStream getErr() {
        return this.err;
    }

    @Override
    public NutsTerminalBase getParent() {
        return null;
    }

    @Override
    public NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        return null;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return false;
    }

    @Override
    public NutsSystemTerminalBase setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver) {
        return this;
    }

    @Override
    public NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history) {
        return this;
    }

    @Override
    public NutsCommandHistory getCommandHistory() {
        return null;
    }

    @Override
    public NutsCommandReadHighlighter getCommandReadHighlighter() {
        return null;
    }

    @Override
    public NutsSystemTerminalBase setCommandReadHighlighter(NutsCommandReadHighlighter commandReadHighlighter) {
        return this;
    }

}
