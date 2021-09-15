package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.InputStream;
import java.util.Scanner;

@NutsPrototype
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

    public DefaultNutsSystemTerminalBase() {

    }

    private NutsLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = workspace.log().setSession(session).of(NutsSystemTerminalBase.class);
        }
        return LOG;
    }

//    @Override
//    public NutsTerminalMode getOutMode() {
//        return outMode;
//    }
//
//    @Override
//    public NutsSystemTerminalBase setOutMode(NutsTerminalMode mode) {
//        if (mode == null) {
//            mode = NutsTerminalMode.FORMATTED;
//        }
//        if (_LOG() != null) {
//            _LOG().with().session(session).level(Level.CONFIG).verb(NutsLogVerb.UPDATE).formatted().log("change terminal Out mode : {0}",
//                    workspace.text().forStyled(mode.id(), NutsTextStyle.primary1())
//            );
//        }
//        FPrint.installStdOut(this.outMode = mode, session);
//        return this;
//    }

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
//                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
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
//                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
//                    getErr()
//            );
//        }
//        return this;
//    }

//    @Override
//    public NutsSystemTerminalBase setErrMode(NutsTerminalMode mode) {
//        if (mode == null) {
//            mode = NutsTerminalMode.FORMATTED;
//        }
//        if (_LOG() != null) {
//            _LOG().with().session(session).level(Level.CONFIG).verb(NutsLogVerb.UPDATE).formatted().log("change terminal Err mode : {0}",
//                    workspace.text().forStyled(mode.id(), NutsTextStyle.primary1())
//            );
//        }
//        FPrint.installStdErr(this.errMode = mode, session);
//        return this;
//    }
//
//    @Override
//    public NutsTerminalMode getErrMode() {
//        return errMode;
//    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        this.session = criteria.getSession();
        this.workspace = session.getWorkspace();
        if (workspace != null) {
            NutsWorkspaceOptions options=session.getWorkspace().boot().getBootOptions();
            NutsTerminalMode terminalMode = options.getTerminalMode();
            if (terminalMode == null) {
                if (options.isBot()) {
                    terminalMode = NutsTerminalMode.FILTERED;
                } else {
                    terminalMode = NutsTerminalMode.FORMATTED;
                }
            }
            this.out = workspace.io().stdout().convertMode(terminalMode);
            this.err = workspace.io().stderr().convertMode(terminalMode);
            this.in = workspace.io().stdin();
            this.scanner = new Scanner(this.in);
        } else {
            //on uninstall do nothing
        }

        return DEFAULT_SUPPORT;
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message,NutsSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = workspace.io().stdout();
        }
        if(message!=null) {
            out.printf("%s", message);
            out.flush();
        }
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message,NutsSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = workspace.io().stdout();
        }
        if(message!=null) {
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
