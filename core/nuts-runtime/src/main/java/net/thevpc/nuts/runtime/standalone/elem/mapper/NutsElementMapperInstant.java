package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

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
        NutsSession session = context.getSession();
        switch (o.type()) {
            case INSTANT: {
                return o.asInstant().get(session);
            }
            case INTEGER: {
                return Instant.ofEpochMilli(o.asInt().get(session));
            }
            case LONG: {
                return Instant.ofEpochMilli(o.asLong().get(session));
            }
            case STRING: {
                return Instant.parse(o.asString().get(session));
            }
        }
        throw new NutsUnsupportedEnumException(session, o.type());
    }
}
