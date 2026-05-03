package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.DefaultNArtifactCall;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class NElementMapperNArtifactCall implements NElementMapper<NArtifactCall> {

    @Override
    public Object toSimple(NElementSerializerContext<NArtifactCall> context) {
        NArtifactCall o = context.instance();
        DefaultNArtifactCall dd = (o instanceof DefaultNArtifactCall) ? (DefaultNArtifactCall) o : new DefaultNArtifactCall(o);
        return context.defaultToSimple(dd, null);
    }

    @Override
    public NElement toElement(NElementSerializerContext<NArtifactCall> context) {
        NArtifactCall o = context.instance();
        DefaultNArtifactCall dd = (o instanceof DefaultNArtifactCall) ? (DefaultNArtifactCall) o : new DefaultNArtifactCall(o);
        return context.defaultCreateElement(dd, null);
    }

    @Override
    public NArtifactCall toObject(NElementDeserializerContext context) {
        NObjectElement object = context.element().asObject().get();
        NId id = (NId) context.toObject(object.get("id").orNull(), NId.class);
        String[] arguments = (String[]) context.toObject(object.get("arguments").orNull(), String[].class);
        Type mapType = NReflectRepository.of().getParametrizedType(
                Map.class, null, new Type[]{String.class, String.class}
        ).getJavaType();
        String scriptName = context.toObject(object.get("scriptName").orNull(), String.class);
        String scriptContent = context.toObject(object.get("scriptContent").orNull(), String.class);

        return new DefaultNArtifactCall(id, Arrays.asList(arguments),scriptName,scriptContent);
    }
}
