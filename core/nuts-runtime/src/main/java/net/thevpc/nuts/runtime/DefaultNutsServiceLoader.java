package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsComponent;
import net.thevpc.nuts.NutsServiceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsWorkspace;

public class DefaultNutsServiceLoader<T extends NutsComponent<B>, B> implements NutsServiceLoader<T, B> {

    private final ClassLoader classLoader;
    private final Class<T> serviceType;
    private final Class<B> criteriaType;
    private final ServiceLoader<T> loader;
    private final NutsWorkspace ws;

    public DefaultNutsServiceLoader(NutsWorkspace ws,Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        this.ws = ws;
        this.classLoader = classLoader;
        this.serviceType = serviceType;
        this.criteriaType = criteriaType;
        loader = ServiceLoader.load(serviceType,
                classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader
        );
    }

    @Override
    public List<T> loadAll(B criteria) {
        List<T> all = new ArrayList<>();
        NutsSupportLevelContext<B> c=new DefaultNutsSupportLevelContext<>(ws,criteria);
        for (T t : loader) {
            int p = t.getSupportLevel(c);
            if (p > NutsComponent.NO_SUPPORT) {
                all.add(t);
            }
        }
        return all;
    }

    @Override
    public T loadBest(B criteria) {
        T best = null;
        int bestVal = NutsComponent.NO_SUPPORT;
        NutsSupportLevelContext<B> c=new DefaultNutsSupportLevelContext<>(ws,criteria);
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
