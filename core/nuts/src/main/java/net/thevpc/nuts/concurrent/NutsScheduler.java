package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.util.NutsUtils;

import java.util.concurrent.ExecutorService;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsScheduler extends NutsComponent {
    static NutsScheduler of(NutsSession session){
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsScheduler.class, true, null);
    }
    ExecutorService executorService();
}
