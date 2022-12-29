package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;

import java.util.concurrent.ExecutorService;

@NComponentScope(NComponentScopeType.WORKSPACE)
public interface NScheduler extends NComponent {
    static NScheduler of(NSession session){
       return NExtensions.of(session).createSupported(NScheduler.class);
    }
    ExecutorService executorService();
}
