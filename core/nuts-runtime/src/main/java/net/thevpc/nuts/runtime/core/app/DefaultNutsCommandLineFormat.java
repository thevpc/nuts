package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;

public class DefaultNutsCommandLineFormat extends DefaultFormatBase<NutsCommandLineFormat> implements NutsCommandLineFormat {

    private NutsCommandLine value;
    private NutsCommandlineFamily formatFamily=NutsCommandlineFamily.DEFAULT;
    private NutsCommandLineFormatStrategy formatStrategy=NutsCommandLineFormatStrategy.DEFAULT;

    public DefaultNutsCommandLineFormat(NutsWorkspace ws) {
        super(ws, "commandline");
    }

    public NutsCommandLineFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(NutsCommandLine value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(String[] args) {
        checkSession();
        return setValue(args == null ? null : getSession().commandLine().create(args));
    }

    @Override
    public NutsCommandLineFormat setValue(String args) {
        return setValue(args == null ? null : getSession().commandLine().parse(args));
    }
    public NutsCommandlineFamily getCommandlineFamily() {
        return formatFamily;
    }

    public NutsCommandLineFormat setCommandlineFamily(NutsCommandlineFamily family) {
        this.formatFamily = family==null?NutsCommandlineFamily.DEFAULT : family;
        return this;
    }

    public NutsCommandLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public void setFormatStrategy(NutsCommandLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy==null?NutsCommandLineFormatStrategy.DEFAULT : formatStrategy;
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
    public void print(NutsPrintStream out) {
        checkSession();
        if (value != null) {
            String cmd =
                    NutsCommandLineBashFamilySupport.of(getCommandlineFamily(), getSession())
                    .escapeArguments(value.toStringArray(),getFormatStrategy(), getSession());
            if (isNtf()) {
                out.print("```sh " + cmd + "```");
            } else {
                out.print(cmd);
            }
        }
    }
}
