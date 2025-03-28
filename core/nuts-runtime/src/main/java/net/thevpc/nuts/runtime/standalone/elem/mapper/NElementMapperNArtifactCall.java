package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.DefaultNArtifactCall;

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
        NObjectElement object = o.asObject().get();
        NId id = (NId) context.elementToObject(object.get("id").orNull(), NId.class);
        String[] arguments = (String[]) context.elementToObject(object.get("arguments").orNull(), String[].class);
        Type mapType = NReflectRepository.of().getParametrizedType(
                Map.class, null, new Type[]{String.class, String.class}
        ).getJavaType();
        String scriptName = context.elementToObject(object.get("scriptName").orNull(), String.class);
        String scriptContent = context.elementToObject(object.get("scriptContent").orNull(), String.class);

        return new DefaultNArtifactCall(id, Arrays.asList(arguments),scriptName,scriptContent);
    }
}
