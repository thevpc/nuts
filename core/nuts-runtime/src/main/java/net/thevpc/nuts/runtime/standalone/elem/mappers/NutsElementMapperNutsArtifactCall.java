package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsArtifactCall;

import java.lang.reflect.Type;
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
        NutsObjectElement object = o.asObject();
        NutsId id = (NutsId) context.elementToObject(object.get(context.elem().ofString("id")), NutsId.class);
        String[] arguments = (String[]) context.elementToObject(object.get(context.elem().ofString("arguments")), String[].class);
        Map<String, String> properties = (Map<String, String>) context
                .elementToObject(object.get(context.elem().
                        ofString("properties")), ReflectUtils.createParametrizedType(Map.class, String.class, String.class));

        return new DefaultNutsArtifactCall(id, arguments, properties);
    }
}
