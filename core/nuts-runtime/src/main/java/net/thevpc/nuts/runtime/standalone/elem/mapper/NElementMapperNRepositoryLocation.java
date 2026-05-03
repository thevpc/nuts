package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.spi.NRepositoryLocation;

import java.lang.reflect.Type;

public class NElementMapperNRepositoryLocation implements NElementMapper<NRepositoryLocation> {

    @Override
    public Object toSimple(NRepositoryLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return o.toString();
    }

    @Override
    public NElement createElement(NRepositoryLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(o.toString());
    }

    @Override
    public NRepositoryLocation createObject(NElementDeserializerContext context) {
        NElement element = context.element();
        return NRepositoryLocation.of(element.asStringValue().get());
    }

}
