package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.InputStream;
import java.util.Scanner;

@NutsComponentScope(NutsComponentScopeType.PROTOTYPE)
public class DefaultNutsSystemTerminalBaseBoot implements NutsSystemTerminalBase {

    private final Scanner scanner;
    //    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
//    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private final NutsPrintStream out;
    private final NutsPrintStream err;
    private final InputStream in;
    private final NutsWorkspace workspace;
    private final NutsSession session;
    private final DefaultNutsBootModel bootModel;
    private NutsLogger LOG;
    private NutsCommandHistory history;
    private String commandHighlighter;
    private NutsCommandAutoCompleteResolver commandAutoCompleteResolver;

    public DefaultNutsSystemTerminalBaseBoot(DefaultNutsBootModel bootModel) {
        this.bootModel = bootModel;
        this.session = bootModel.bootSession();
        this.workspace = session.getWorkspace();
//        NutsTerminalMode terminalMode = options.getTerminalMode();
//        if (terminalMode == null) {
//            if (options.isBot()) {
//                terminalMode = NutsTerminalMode.FILTERED;
//            } else {
//                terminalMode = NutsTerminalMode.FORMATTED;
//            }
//        }
//        this.outMode = terminalMode;
//        this.errMode = terminalMode;
//        this.outMode = bootModel.stdout().mode();
//        this.errMode = bootModel.stderr().mode();
        this.out = bootModel.stdout();//.convertMode(terminalMode);
        this.err = bootModel.stderr();//.convertMode(terminalMode);
        this.in = bootModel.stdin();
        this.scanner = new Scanner(this.in);
    }


    private NutsLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = NutsLogger.of(NutsSystemTerminalBase.class, session);
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
//                    NutsTexts.of(session).toText(NutsMessage.cstyle(prompt,params)).toString(),
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
//                    NutsTexts.of(session).toText(NutsMessage.cstyle(prompt,params)).toString(),
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

}
