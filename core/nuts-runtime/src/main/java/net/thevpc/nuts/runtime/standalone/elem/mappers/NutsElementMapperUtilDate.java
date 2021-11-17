package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class NutsElementMapperUtilDate implements NutsElementMapper<Date> {

    @Override
    public Object destruct(Date src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Date o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().forInstant(o.toInstant());
    }

    @Override
    public Date createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        Instant i = (Instant) context.defaultElementToObject(o, Instant.class);
        return new Date(i.toEpochMilli());
    }
}
