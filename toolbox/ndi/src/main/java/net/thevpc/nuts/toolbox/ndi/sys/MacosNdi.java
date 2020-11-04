package net.thevpc.nuts.toolbox.ndi.sys;

import net.thevpc.nuts.toolbox.ndi.base.AnyNixNdi;
import net.thevpc.nuts.NutsApplicationContext;

public class MacosNdi extends AnyNixNdi {
    public MacosNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public String getBashrcName() {
        return ".bash_profile";
    }
}
