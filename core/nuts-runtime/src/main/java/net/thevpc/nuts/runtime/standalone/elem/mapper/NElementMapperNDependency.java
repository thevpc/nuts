package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyBuilder;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.text.NDependencyWriter;
import net.thevpc.nuts.runtime.standalone.DefaultNDependencyBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.text.NObjectWriter;
import net.thevpc.nuts.text.NText;

import java.lang.reflect.Type;

public class NElementMapperNDependency implements NElementMapper<NDependency> {

    @Override
    public Object toSimple(NElementSerializerContext<NDependency> context) {
        NDependency o = context.instance();
        if (o.getExclusions().isEmpty()) {
            //use compact form
            if (context.isNtf()) {
                return NDependencyWriter.of().setNtf(true).format(o);
            } else {

                return context.defaultToSimple(NObjectWriter.of(o)
                        .setNtf(context.isNtf())
                        .format(o), null);
            }
        }
        return context.defaultToSimple(NDependencyBuilder.of().copyFrom(o), null);
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDependency> context) {
        NDependency o = context.instance();
        NText format = NObjectWriter.of(o)
                .setNtf(context.isNtf())
                .format(o);
        return context.defaultCreateElement(format, null);
    }

    @Override
    public NDependency toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        if (element.type().isAnyString()) {
            return NDependency.get(element.asStringValue().get()).get();
        }
        NDependencyBuilder builder = context.defaultToObject(element, DefaultNDependencyBuilder.class);
        return NDependencyBuilder.of().copyFrom(builder).build();
    }

}
