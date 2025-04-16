package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

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
        switch (o.type()) {
            case INSTANT: {
                return o.asLiteral().asInstant().get();
            }
            case INTEGER: {
                return Instant.ofEpochMilli(o.asLiteral().asInt().get());
            }
            case LONG: {
                return Instant.ofEpochMilli(o.asLiteral().asLong().get());
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            {
                return Instant.parse(o.asStringValue().get());
            }
        }
        throw new NUnsupportedEnumException(o.type());
    }
}
