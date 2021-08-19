package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.sys.unix;

import net.thevpc.nuts.NutsSession;

public class MacosNdi extends AnyNixNdi {
    public MacosNdi(NutsSession session) {
        super(session);
    }

    public String getBashrcName() {
        return ".bash_profile";
    }

}
