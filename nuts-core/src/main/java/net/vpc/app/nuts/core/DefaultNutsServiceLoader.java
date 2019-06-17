package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsComponent;
import net.vpc.app.nuts.NutsServiceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class DefaultNutsServiceLoader<T extends NutsComponent<B>, B> implements NutsServiceLoader<T, B> {

    private ClassLoader classLoader;
    private Class<T> serviceType;
    private Class<B> criteriaType;
    private ServiceLoader<T> loader;

    public DefaultNutsServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
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
        for (T t : loader) {
            int p = t.getSupportLevel(criteria);
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
        for (T t : loader) {
            int p = t.getSupportLevel(criteria);
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
