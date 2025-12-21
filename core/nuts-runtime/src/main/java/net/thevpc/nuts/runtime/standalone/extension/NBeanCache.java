package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.ext.NFactoryException;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NScorableContext;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NBeanCache {
    private final Map<TypeAndArgTypes, NBeanConstructor> cachedConstructors = new HashMap<>();
    private final NLog LOG;

    public NBeanCache(NLog LOG) {
        this.LOG = LOG;
    }

    public <T> NBeanConstructor<T> findConstructor(Class<T> implType, Class[] argTypes, Class apiType, NBeanConstructorContext context) {
        TypeAndArgTypes tt = new TypeAndArgTypes(implType, argTypes, apiType);
        synchronized (cachedConstructors) {
            NBeanConstructor<T> o = cachedConstructors.get(tt);
            if (o != null) {
                return o;
            }
            NBeanConstructor<T> c = NBeanConstructorHelper.createConstructor(tt, context, LOG);
            cachedConstructors.put(tt, c);
            return c;
        }
    }
}

