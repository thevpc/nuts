package net.thevpc.nuts.runtime.standalone.extensions;

import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsServiceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import net.thevpc.nuts.spi.NutsDefaultSupportLevelContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsServiceLoader<T extends NutsComponent, B> implements NutsServiceLoader<T> {

    private final ClassLoader classLoader;
    private final Class<T> serviceType;
    private final Class<B> criteriaType;
    private final ServiceLoader<T> loader;
    private final NutsSession session;

    public DefaultNutsServiceLoader(NutsSession session,Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
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
        NutsSupportLevelContext c=new NutsDefaultSupportLevelContext(session,criteria);
        for (T t : loader) {
            int p = t.getSupportLevel(c);
            if (p > NutsComponent.NO_SUPPORT) {
                all.add(t);
            }
        }
        return all;
    }

    @Override
    public T loadBest(Object criteria) {
        T best = null;
        int bestVal = NutsComponent.NO_SUPPORT;
        NutsSupportLevelContext c=new NutsDefaultSupportLevelContext(session,criteria);
        for (T t : loader) {
            int p = t.getSupportLevel(c);
            if (p > NutsComponent.NO_SUPPORT) {
                if (best == null || bestVal < p) {
                    best = t;
                    bestVal = p;
                }
            }
        }
        return best;
    }
}
