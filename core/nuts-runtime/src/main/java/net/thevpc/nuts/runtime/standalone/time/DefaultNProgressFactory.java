package net.thevpc.nuts.runtime.standalone.time;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;

public class DefaultNProgressFactory implements NProgressFactory {
    private NWorkspace workspace;

    public DefaultNProgressFactory(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NProgressListener createProgressListener(Object source, Object sourceOrigin) {
        if (!acceptMonitoring(source, sourceOrigin)) {
            return null;
        }
        return new DefaultNCountProgressListener();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin) {
        if (!workspace.currentSession().isProgress()) {
            return false;
        }
        return true;
    }
}
