package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.DefaultNutsArtifactCall;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class NutsElementMapperNutsArtifactCall implements NutsElementMapper<NutsArtifactCall> {

    @Override
    public Object destruct(NutsArtifactCall o, Type typeOfSrc, NutsElementFactoryContext context) {
        DefaultNutsArtifactCall dd = (o instanceof DefaultNutsArtifactCall) ? (DefaultNutsArtifactCall) o : new DefaultNutsArtifactCall(o);
        return context.defaultDestruct(dd, null);
    }

    @Override
    public NutsElement createElement(NutsArtifactCall o, Type typeOfSrc, NutsElementFactoryContext context) {
        DefaultNutsArtifactCall dd = (o instanceof DefaultNutsArtifactCall) ? (DefaultNutsArtifactCall) o : new DefaultNutsArtifactCall(o);
        return context.defaultObjectToElement(dd, null);
    }

    @Override
    public NutsArtifactCall createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsObjectElement object = o.asObject().get(session);
        NutsId id = (NutsId) context.elementToObject(object.get(context.elem().ofString("id")).orNull(), NutsId.class);
        String[] arguments = (String[]) context.elementToObject(object.get(context.elem().ofString("arguments")).orNull(), String[].class);
        Map<String, String> properties = (Map<String, String>) context
                .elementToObject(object.get(context.elem().
                        ofString("properties")).orNull(), ReflectUtils.createParametrizedType(Map.class, String.class, String.class));

        return new DefaultNutsArtifactCall(id, Arrays.asList(arguments), properties);
    }
}
