package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NshOptions {
    private List<String> boot_nonOptions = new ArrayList<>();
    private boolean boot_interactive = false;
    private boolean boot_command = false;
    private boolean verbose = false;
    private boolean xtrace = false;

    public void parse(NutsCommandLine cmd, NutsApplicationContext appContext) {
        NutsArgument a;
        while (cmd.hasNext()) {
            if (getBoot_nonOptions().isEmpty()) {
                if ((a = cmd.next("--help")) != null) {
                    setBoot_command(true);
                    getBoot_nonOptions().add("help");
                } else if (appContext != null && appContext.configureFirst(cmd)) {
                    //ok
                } else if ((a = cmd.nextString("-c", "--command")) != null) {
                    setBoot_command(true);
                    String cc = a.getStringValue();
                    if (StringUtils.isBlank(cc)) {
                        cmd.required("missing command for -c");
                    }
                    getBoot_nonOptions().add(cc);
                    getBoot_nonOptions().addAll(Arrays.asList(cmd.toStringArray()));
                    cmd.skipAll();
                } else if ((a = cmd.nextBoolean("-i", "--interactive")) != null) {
                    setBoot_interactive(a.getBooleanValue());
                } else if ((a = cmd.nextBoolean("-x")) != null) {
                    setXtrace((a.getBooleanValue()));
                } else if (cmd.peek().isOption()) {
                    cmd.setCommandName("nsh").unexpectedArgument();
                } else {
                    getBoot_nonOptions().add(cmd.next().getString());
                }
            } else {
                getBoot_nonOptions().add(cmd.next().getString());
            }
        }
        if (getBoot_nonOptions().isEmpty()) {
            setBoot_interactive(true);
        }
    }

    public List<String> getBoot_nonOptions() {
        return boot_nonOptions;
    }

    public NshOptions setBoot_nonOptions(List<String> boot_nonOptions) {
        this.boot_nonOptions = boot_nonOptions;
        return this;
    }

    public boolean isBoot_interactive() {
        return boot_interactive;
    }

    public NshOptions setBoot_interactive(boolean boot_interactive) {
        this.boot_interactive = boot_interactive;
        return this;
    }

    public boolean isBoot_command() {
        return boot_command;
    }

    public NshOptions setBoot_command(boolean boot_command) {
        this.boot_command = boot_command;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public NshOptions setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isXtrace() {
        return xtrace;
    }

    public NshOptions setXtrace(boolean xtrace) {
        this.xtrace = xtrace;
        return this;
    }
}
