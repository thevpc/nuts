package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsCommandLineFormat;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

import java.io.PrintStream;

public class DefaultNutsCommandLineFormat extends DefaultFormatBase<NutsCommandLineFormat> implements NutsCommandLineFormat {

    private NutsCommandLine value;
    private boolean ntf = true;

    public DefaultNutsCommandLineFormat(NutsWorkspace ws) {
        super(ws, "commandline");
    }

    public boolean isNtf() {
        return ntf;
    }

    public NutsCommandLineFormat setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(NutsCommandLine value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(String[] args) {
        return setValue(args == null ? null : getWorkspace().commandLine().create(args));
    }

    @Override
    public NutsCommandLineFormat setValue(String args) {
        return setValue(args == null ? null : getWorkspace().commandLine().parse(args));
    }

    @Override
    public NutsCommandLine getValue() {
        return value;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

    @Override
    public void print(PrintStream out) {
        if (value != null) {
            String cmd = NutsCommandLineUtils.escapeArguments(value.toStringArray());
            if (isNtf()) {
                out.print("```sh " + cmd + "```");
            } else {
                out.print(cmd);
            }
        }
    }
}
