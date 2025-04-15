package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class NElementMapperUtilDate implements NElementMapper<Date> {

    @Override
    public Object destruct(Date src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Date o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofInstant(o.toInstant());
    }

    @Override
    public Date createObject(NElement o, Type to, NElementFactoryContext context) {
        Instant i = (Instant) context.defaultElementToObject(o, Instant.class);
        return new Date(i.toEpochMilli());
    }
}
