package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

import java.util.concurrent.ExecutorService;

public interface NScheduler extends NComponent {
    static NScheduler of(NSession session){
       return NExtensions.of(session).createComponent(NScheduler.class).get();
    }
    ExecutorService executorService();
}
