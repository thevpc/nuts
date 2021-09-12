package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.unix;

import net.thevpc.nuts.NutsSession;

public class ZshNdi extends AnyNixNdi {
    public ZshNdi(NutsSession session) {
        super(session);
    }

    @Override
    public String getBashrcName() {
        return ".zshrc";
    }
}
