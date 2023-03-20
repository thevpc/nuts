package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.DefaultNArtifactCall;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class NElementMapperNArtifactCall implements NElementMapper<NArtifactCall> {

    @Override
    public Object destruct(NArtifactCall o, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNArtifactCall dd = (o instanceof DefaultNArtifactCall) ? (DefaultNArtifactCall) o : new DefaultNArtifactCall(o);
        return context.defaultDestruct(dd, null);
    }

    @Override
    public NElement createElement(NArtifactCall o, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNArtifactCall dd = (o instanceof DefaultNArtifactCall) ? (DefaultNArtifactCall) o : new DefaultNArtifactCall(o);
        return context.defaultObjectToElement(dd, null);
    }

    @Override
    public NArtifactCall createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        NObjectElement object = o.asObject().get(session);
        NId id = (NId) context.elementToObject(object.get(context.elem().ofString("id")).orNull(), NId.class);
        String[] arguments = (String[]) context.elementToObject(object.get(context.elem().ofString("arguments")).orNull(), String[].class);
        Type mapType = context.getReflectRepository().getParametrizedType(
                Map.class, null, new Type[]{String.class, String.class}
        ).getJavaType();
        Map<String, String> properties = (Map<String, String>) context
                .elementToObject(object.get(context.elem().
                        ofString("properties")).orNull(), mapType);

        return new DefaultNArtifactCall(id, Arrays.asList(arguments), properties);
    }
}
