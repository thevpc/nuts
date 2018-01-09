package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NutsTerminalDelegate implements NutsTerminal {

    private NutsTerminal base;
    private InputStream inReplace;
    private BufferedReader inReplaceReader;
    private NutsPrintStream outReplace;
    private NutsPrintStream errReplace;

    public NutsTerminalDelegate(NutsTerminal base, InputStream inReplace, NutsPrintStream outReplace, NutsPrintStream errReplace) {
        if (base instanceof NutsTerminalDelegate) {
            base = ((NutsTerminalDelegate) base).base;
        }
        this.base = base;
        InputStream in0 = base.getIn();
        if (inReplace!=null && in0 != inReplace) {
            this.inReplace = inReplace;
            this.inReplaceReader = inReplace == null ? null : new BufferedReader(new InputStreamReader(inReplace));
        }
        this.outReplace = outReplace;
        this.errReplace = errReplace;
    }

    @Override
    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) throws IOException {

    }

    @Override
    public void setCommandContext(NutsCommandContext context) {
        base.setCommandContext(context);
    }

    @Override
    public String readLine(String prompt) throws IOException {
        if (inReplaceReader != null) {
            getOut().print(prompt);
            return inReplaceReader.readLine();
        }
        return base.readLine(prompt);
    }

    @Override
    public String readPassword(String prompt) throws IOException {
        if (inReplaceReader != null) {
            getOut().print(prompt);
            return inReplaceReader.readLine();
        }
        return base.readPassword(prompt);
    }

    @Override
    public InputStream getIn() {
        if (inReplace != null) {
            return inReplace;
        }
        return base.getIn();
    }

    @Override
    public void setIn(InputStream in) {
        inReplace = in;
    }

    @Override
    public NutsPrintStream getOut() {
        if (outReplace != null) {
            return outReplace;
        }
        return base.getOut();
    }

    @Override
    public void setOut(NutsPrintStream out) {
        outReplace = out;
    }

    @Override
    public NutsPrintStream getErr() {
        if (errReplace != null) {
            return errReplace;
        }
        return base.getErr();
    }

    @Override
    public void setErr(NutsPrintStream err) {
        errReplace = err;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return base.getSupportLevel(criteria);
    }
}
