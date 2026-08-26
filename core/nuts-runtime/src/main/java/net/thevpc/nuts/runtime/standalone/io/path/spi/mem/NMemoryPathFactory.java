package net.thevpc.nuts.runtime.standalone.io.path.spi.mem;

import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;

public class NMemoryPathFactory implements NPathFactorySPI {
    @Override
    public NScoredCallable<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
        NMemoryPathStore store = NWorkspace.of().getOrComputeProperty(NMemoryPathStore.class, NMemoryPathStore::new);
        try {
            if (path.startsWith(NMemoryPathStore.PREFIX)) {
                return NScoredCallable.of(NScorable.DEFAULT_SCORE, () -> new NMemFileSPI(path, store));
            }
        } catch (Exception ex) {
            //ignore
        }
        return null;
    }

    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static int getScore(NScorableContext context) {
        Object cri = context.criteria();
        if (!(cri instanceof String)) {
            return NScorable.DEFAULT_SCORE;
        }
        String path = (String) cri;
        try {
            if (path.startsWith(NMemoryPathStore.PREFIX)) {
                return NScorable.DEFAULT_SCORE;
            }
        } catch (Exception ex) {
            //ignore
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
