package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;

import java.util.concurrent.ExecutorService;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsScheduler extends NutsComponent {
    static NutsScheduler of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsScheduler.class, true, null);
    }
    ExecutorService executorService();
}
