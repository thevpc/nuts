package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;
import java.time.Instant;

public class NElementMapperInstant implements NElementMapper<Instant> {

    @Override
    public Object destruct(Instant src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Instant o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofInstant((Instant) o);
    }

    @Override
    public Instant createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        switch (o.type()) {
            case INSTANT: {
                return o.asInstant().get();
            }
            case INTEGER: {
                return Instant.ofEpochMilli(o.asInt().get());
            }
            case LONG: {
                return Instant.ofEpochMilli(o.asLong().get());
            }
            case STRING: {
                return Instant.parse(o.asString().get());
            }
        }
        throw new NUnsupportedEnumException(o.type());
    }
}
