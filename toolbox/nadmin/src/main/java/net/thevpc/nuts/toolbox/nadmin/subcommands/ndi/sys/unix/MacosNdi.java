package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.unix;

import net.thevpc.nuts.NutsApplicationContext;

public class MacosNdi extends AnyNixNdi {
    public MacosNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public String getBashrcName() {
        return ".bash_profile";
    }
}
