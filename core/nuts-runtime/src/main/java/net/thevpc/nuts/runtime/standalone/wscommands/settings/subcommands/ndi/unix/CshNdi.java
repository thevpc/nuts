package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.unix;

import net.thevpc.nuts.NutsSession;

public class CshNdi extends AnyNixNdi {
    public CshNdi(NutsSession session) {
        super(session);
    }

    @Override
    public String getBashrcName() {
        return ".cshrc";
    }
}
