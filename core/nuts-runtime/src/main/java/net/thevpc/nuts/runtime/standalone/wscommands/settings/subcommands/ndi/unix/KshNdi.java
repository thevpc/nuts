package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.unix;

import net.thevpc.nuts.NutsSession;

public class KshNdi extends AnyNixNdi {
    public KshNdi(NutsSession session) {
        super(session);
    }

    @Override
    public String getBashrcName() {
        return ".kshrc";
    }
}
