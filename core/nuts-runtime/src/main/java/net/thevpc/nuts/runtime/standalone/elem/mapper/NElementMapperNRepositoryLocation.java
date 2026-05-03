package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.spi.NRepositoryLocation;

import java.lang.reflect.Type;

public class NElementMapperNRepositoryLocation implements NElementMapper<NRepositoryLocation> {

    @Override
    public Object toSimple(NElementSerializerContext<NRepositoryLocation> context) {
        return context.instance().toString();
    }

    @Override
    public NElement toElement(NElementSerializerContext<NRepositoryLocation> context) {
        return NElement.ofString(context.instance().toString());
    }

    @Override
    public NRepositoryLocation toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        return NRepositoryLocation.of(element.asStringValue().get());
    }

}
