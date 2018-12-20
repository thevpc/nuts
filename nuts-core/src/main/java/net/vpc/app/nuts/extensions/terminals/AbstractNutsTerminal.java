package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.DefaultNutsResponseParser;

import java.io.*;
import java.util.Arrays;

public abstract class AbstractNutsTerminal implements NutsTerminal {
    protected PrintStream out_getFormatted_Force;
    protected PrintStream out_getFormatted_NoForce;
    protected PrintStream err_getFormatted_Force;
    protected PrintStream err_getFormatted_NoForce;
    protected NutsWorkspace workspace;
    protected PrintStream outReplace;
    protected PrintStream errReplace;
    protected InputStream inReplace;
    protected BufferedReader reader;
    protected InputStream in;
    protected PrintStream out;
    protected PrintStream err;
    protected BufferedReader inReplaceReader;

    @Override
    public void install(NutsWorkspace workspace, InputStream in, PrintStream out, PrintStream err) {
        this.workspace = workspace;
    }

    @Override
    public PrintStream getFormattedOut() {
        return getFormattedOut(false);
    }

    @Override
    public PrintStream getFormattedErr() {
        return getFormattedErr(false);
    }

    @Override
    public PrintStream getFormattedOut(boolean forceNoColors) {
        if (forceNoColors) {
            if (out_getFormatted_Force == null) {
                out_getFormatted_Force = workspace.createPrintStream(getOut(), true, forceNoColors);
            }
            return out_getFormatted_Force;
        } else {
            if (out_getFormatted_NoForce == null) {
                out_getFormatted_NoForce = workspace.createPrintStream(getOut(), true, forceNoColors);
            }
            return out_getFormatted_NoForce;
        }
    }

    @Override
    public PrintStream getFormattedErr(boolean forceNoColors) {
        if (forceNoColors) {
            if (err_getFormatted_Force == null) {
                err_getFormatted_Force = workspace.createPrintStream(getErr(), true, forceNoColors);
            }
            return err_getFormatted_Force;
        } else {
            if (err_getFormatted_NoForce == null) {
                err_getFormatted_NoForce = workspace.createPrintStream(getErr(), true, forceNoColors);
            }
            return err_getFormatted_NoForce;
        }
    }

    @Override
    public InputStream getIn() {
        if (inReplace != null) {
            return inReplace;
        }
        return in;
    }

    @Override
    public PrintStream getOut() {
        if (outReplace != null) {
            return outReplace;
        }
        return out;
    }

    @Override
    public PrintStream getErr() {
        if (errReplace != null) {
            return errReplace;
        }
        return err;
    }

    @Override
    public void setIn(InputStream in) {
        inReplace = in;
        if (in == null) {
            inReplaceReader = null;
        } else {
            inReplaceReader = new BufferedReader(new InputStreamReader(inReplace));
        }
    }

    @Override
    public void setOut(PrintStream out) {
        outReplace = out;
        out_getFormatted_Force = null;
        out_getFormatted_NoForce = null;
    }

    @Override
    public void setErr(PrintStream err) {
        errReplace = err;
        err_getFormatted_Force = null;
        err_getFormatted_NoForce = null;
    }

    public BufferedReader getReader() {
        if (inReplaceReader != null) {
            return inReplaceReader;
        }
        return reader;
    }

    @Override
    public String readLine(String promptFormat, Object ... params) {
        getOut().printf(promptFormat, params);
        getIn();
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    public boolean isInOverridden(){
        return inReplaceReader!=null;
    }

    public boolean isOutOverridden(){
        return outReplace!=null;
    }

    public boolean isErrOverridden(){
        return errReplace!=null;
    }

    @Override
    public String readPassword(String prompt) {
        return CoreIOUtils.readPassword(prompt, getIn(), getOut());
    }

    protected void copyFrom(AbstractNutsTerminal other){
        this.workspace=other.workspace;
        this.in=other.in;
        this.reader=other.reader;
        this.out=other.out;
        this.err=other.err;
        this.out_getFormatted_Force=other.out_getFormatted_Force;
        this.out_getFormatted_NoForce=other.out_getFormatted_NoForce;
        this.err_getFormatted_Force=other.err_getFormatted_Force;
        this.err_getFormatted_NoForce=other.err_getFormatted_NoForce;
        this.inReplace=other.inReplace;
        this.inReplaceReader=other.inReplaceReader;
        this.outReplace=other.outReplace;
        this.errReplace=other.errReplace;
    }
    @Override
    public <T> T ask(NutsQuestion<T> question) {
        String message = question.getMessage();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        getFormattedOut().printf(message, question.getMessageParameters());
        if (question.getDefautValue() != null) {
            getFormattedOut().printf(" (default is [[%s]])", question.getDefautValue());
        }
        if (question.getAcceptedValues() != null && question.getAcceptedValues().length > 0) {
            getFormattedOut().printf(" (accepts [[%s]])", Arrays.toString(question.getAcceptedValues()));
        }
        getFormattedOut().printf("\n");
        String v = readLine("\tPlease enter value or @@%s@@ to cancel : ", "cancel!");
        if ("cancel!".equals(v)) {
            throw new NutsUserCancelException();
        }
        NutsResponseParser p = question.getParse();
        if (p == null) {
            p = DefaultNutsResponseParser.INSTANCE;
        }
        if (v == null || v.length() == 0) {
            return (T) p.parse(question.getDefautValue(),question.getValueType());
        }
        return (T) p.parse(v,question.getValueType());
    }
}
