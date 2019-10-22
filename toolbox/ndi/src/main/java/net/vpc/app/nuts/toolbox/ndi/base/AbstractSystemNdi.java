package net.vpc.app.nuts.toolbox.ndi.base;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.ndi.SystemNdi;

import java.util.logging.Logger;

public abstract class AbstractSystemNdi implements SystemNdi {
    public static final Logger LOG = Logger.getLogger(AbstractSystemNdi.class.getName());
    protected NutsApplicationContext context;

    public AbstractSystemNdi(NutsApplicationContext appContext) {
        this.context = appContext;
    }
}
