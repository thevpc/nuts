package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NScorableQueryImpl;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.io.NServiceLoader;

import java.util.List;
import java.util.ServiceLoader;
import net.thevpc.nuts.spi.NDefaultScorableContext;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

public class DefaultNServiceLoader<T extends NComponent, B> implements NServiceLoader<T> {

    private final ClassLoader classLoader;
    private final Class<T> serviceType;
    private final Class<B> criteriaType;
    private final ServiceLoader<T> loader;
    private final NWorkspace workspace;

    public DefaultNServiceLoader(NWorkspace workspace, Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        this.workspace = workspace;
        this.classLoader = classLoader;
        this.serviceType = serviceType;
        this.criteriaType = criteriaType;
        loader = ServiceLoader.load(serviceType,
                classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader
        );
    }

    @Override
    public List<T> loadAll(Object criteria) {
        return new NScorableQueryImpl<T>(NScorableContext.of(criteria))
                .fromIterable(loader)
                .withName(NMsg.ofC("component %s",serviceType))
                .getAll();
    }

    @Override
    public NOptional<T> loadBest(Object criteria) {
        return new NScorableQueryImpl<T>(NScorableContext.of(criteria))
                .fromIterable(loader)
                .withName(NMsg.ofC("component %s",serviceType))
                .getBest();
    }
}
