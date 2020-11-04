package net.thevpc.nuts.toolbox.ndi.base;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.toolbox.ndi.SystemNdi;

import java.util.logging.Logger;

public abstract class AbstractSystemNdi implements SystemNdi {
    public static final Logger LOG = Logger.getLogger(AbstractSystemNdi.class.getName());
    protected NutsApplicationContext context;

    public AbstractSystemNdi(NutsApplicationContext appContext) {
        this.context = appContext;
    }
}
