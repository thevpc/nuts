package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.io.NServiceLoader;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NScoredValue;

public class DefaultNServiceLoader<T extends NComponent, B> implements NServiceLoader<T> {

    private final ClassLoader classLoader;
    private final Class<T> serviceType;
    private final Class<B> criteriaType;

    public DefaultNServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.serviceType = serviceType;
        this.criteriaType = criteriaType;

    }

    @Override
    public List<T> loadAll(Object criteria) {
        List<T> all = new ArrayList<>();
        NScorableContext context = NScorableContext.of(criteria);
        NExtensions nExtensions = NExtensions.of();
        for (Class<? extends T> t : ServiceTypeIterator.loadList(serviceType, classLoader)) {
            NScoredValue<T> s = nExtensions.getTypeScoredValue(t, serviceType, context);
            if (s.isValid()) {
                try {
                    all.add(s.value());
                }catch (Exception ex) {
                    NLog.of(DefaultNServiceLoader.class).log(NMsg.ofC("unable to resolve service type %s : %s",serviceType.getName(),ex));
                }
            }
        }
        return all;
    }

    @Override
    public NOptional<T> loadBest(Object criteria) {
        NScorableContext context = NScorableContext.of(criteria);
        NExtensions nExtensions = NExtensions.of();
        T bestInstance = null;
        int bestScore = -1;
        for (Class<? extends T> t : ServiceTypeIterator.loadList(serviceType, classLoader)) {
            NScoredValue<T> s = nExtensions.getTypeScoredValue(t, serviceType, context);
            int score = s.score();
            if (score>0) {
                if(bestScore<0 || score>bestScore) {
                    T i2=null;
                    try {
                        i2 = s.value();
                    }catch (Exception ex) {
                        NLog.of(DefaultNServiceLoader.class).log(NMsg.ofC("unable to resolve service type %s : %s",serviceType.getName(),ex));
                    }
                    if(i2!=null) {
                        bestScore = score;
                        bestInstance = i2;
                    }
                }
            }
        }
        return NOptional.ofNamed(bestInstance,NMsg.ofC("component %s", serviceType));
    }
}
