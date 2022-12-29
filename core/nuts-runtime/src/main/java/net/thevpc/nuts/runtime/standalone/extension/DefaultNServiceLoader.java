package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.io.NServiceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import net.thevpc.nuts.spi.NDefaultSupportLevelContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNServiceLoader<T extends NComponent, B> implements NServiceLoader<T> {

    private final ClassLoader classLoader;
    private final Class<T> serviceType;
    private final Class<B> criteriaType;
    private final ServiceLoader<T> loader;
    private final NSession session;

    public DefaultNServiceLoader(NSession session, Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        this.session = session;
        this.classLoader = classLoader;
        this.serviceType = serviceType;
        this.criteriaType = criteriaType;
        loader = ServiceLoader.load(serviceType,
                classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader
        );
    }

    @Override
    public List<T> loadAll(Object criteria) {
        List<T> all = new ArrayList<>();
        NSupportLevelContext c=new NDefaultSupportLevelContext(session,criteria);
        for (T t : loader) {
            int p = t.getSupportLevel(c);
            if (p > NComponent.NO_SUPPORT) {
                all.add(t);
            }
        }
        return all;
    }

    @Override
    public T loadBest(Object criteria) {
        T best = null;
        int bestVal = NComponent.NO_SUPPORT;
        NSupportLevelContext c=new NDefaultSupportLevelContext(session,criteria);
        for (T t : loader) {
            int p = t.getSupportLevel(c);
            if (p > NComponent.NO_SUPPORT) {
                if (best == null || bestVal < p) {
                    best = t;
                    bestVal = p;
                }
            }
        }
        return best;
    }
}
