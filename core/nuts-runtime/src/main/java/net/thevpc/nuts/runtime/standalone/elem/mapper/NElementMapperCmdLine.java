package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.builder.NElementSerializerContextImpl;

import java.lang.reflect.Type;

public class NElementMapperCmdLine implements NElementMapper<NCmdLine> {

    @Override
    public Object toSimple(NElementSerializerContext<NCmdLine> context) {
        return context.instance().toStringArray();
    }

    @Override
    public NElement toElement(NElementSerializerContext<NCmdLine> context) {
        return context.defaultCreateElement(this.toSimple(NElementSerializerContextImpl.of(context.instance(), null, context)), null);
    }

    @Override
    public NCmdLine toObject(NElementDeserializerContext context) {
        String[] i = context.defaultToObject(context.element(), String[].class);
        return NCmdLine.of(i);
    }
}
