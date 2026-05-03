package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.elem.mapper.builder.NElementSerializerContextImpl;

import java.lang.reflect.Type;

public class NElementMapperNPath implements NElementMapper<NPath> {

    @Override
    public Object toSimple(NElementSerializerContext<NPath> context) {
        return context.instance().toString();
    }

    @Override
    public NElement toElement(NElementSerializerContext<NPath> context) {
        return context.defaultCreateElement(this.toSimple(NElementSerializerContextImpl.of(context.instance(), null, context)), null);
    }

    @Override
    public NPath toObject(NElementDeserializerContext context) {
        String i = context.defaultToObject(context.element(), String.class);
        return NPath.of(i);
    }
}
