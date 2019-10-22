package net.vpc.app.nuts.toolbox.ndi.sys;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.ndi.base.AnyNixNdi;

public class MacosNdi extends AnyNixNdi {
    public MacosNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public String getBashrcName() {
        return ".bash_profile";
    }
}
