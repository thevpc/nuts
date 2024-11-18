package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.concurrent.ExecutorService;

public interface NScheduler extends NComponent {
    static NScheduler of(){
       return NExtensions.of().createComponent(NScheduler.class).get();
    }
    ExecutorService executorService();
}
