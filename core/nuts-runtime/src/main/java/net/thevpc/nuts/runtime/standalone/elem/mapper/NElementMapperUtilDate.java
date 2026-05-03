package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class NElementMapperUtilDate implements NElementMapper<Date> {

    @Override
    public Object toSimple(Date src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Date o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofInstant(o.toInstant());
    }

    @Override
    public Date createObject(NElementDeserializerContext context) {
        Instant i = (Instant) context.defaultToObject(context.element(), Instant.class);
        return new Date(i.toEpochMilli());
    }
}
