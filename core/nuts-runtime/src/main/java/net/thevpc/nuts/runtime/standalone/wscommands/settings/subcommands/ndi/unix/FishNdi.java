package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.unix;

import net.thevpc.nuts.NutsSession;

public class FishNdi extends AnyNixNdi {
    public FishNdi(NutsSession session) {
        super(session);
    }

    @Override
    public String getBashrcName() {
        return ".config/fish/config.fish";
    }
}
