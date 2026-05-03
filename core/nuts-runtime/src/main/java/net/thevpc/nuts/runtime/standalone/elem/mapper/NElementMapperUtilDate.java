package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class NElementMapperUtilDate implements NElementMapper<Date> {

    @Override
    public Object toSimple(NElementSerializerContext<Date> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<Date> context) {
        return NElement.ofInstant(context.instance().toInstant());
    }

    @Override
    public Date toObject(NElementDeserializerContext context) {
        Instant i = context.defaultToObject(context.element(), Instant.class);
        return new Date(i.toEpochMilli());
    }
}
