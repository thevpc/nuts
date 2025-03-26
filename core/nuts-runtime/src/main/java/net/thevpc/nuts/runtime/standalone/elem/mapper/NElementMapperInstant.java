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
                return o.asInstantValue().get();
            }
            case INTEGER: {
                return Instant.ofEpochMilli(o.asIntValue().get());
            }
            case LONG: {
                return Instant.ofEpochMilli(o.asLongValue().get());
            }
            case STRING: {
                return Instant.parse(o.asStringValue().get());
            }
        }
        throw new NUnsupportedEnumException(o.type());
    }
}
