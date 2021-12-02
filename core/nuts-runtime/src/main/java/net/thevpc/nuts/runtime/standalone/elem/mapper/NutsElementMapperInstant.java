package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsUnsupportedEnumException;

import java.lang.reflect.Type;
import java.time.Instant;

public class NutsElementMapperInstant implements NutsElementMapper<Instant> {

    @Override
    public Object destruct(Instant src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Instant o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofInstant((Instant) o);
    }

    @Override
    public Instant createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        switch (o.type()) {
            case INSTANT: {
                return o.asPrimitive().getInstant();
            }
            case INTEGER: {
                return Instant.ofEpochMilli(o.asPrimitive().getInt());
            }
            case LONG: {
                return Instant.ofEpochMilli(o.asPrimitive().getLong());
            }
            case STRING: {
                return Instant.parse(o.asPrimitive().getString());
            }
        }
        throw new NutsUnsupportedEnumException(context.getSession(), o.type());
    }
}
