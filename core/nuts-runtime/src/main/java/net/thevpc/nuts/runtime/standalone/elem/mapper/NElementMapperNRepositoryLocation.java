package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.spi.NRepositoryLocation;

import java.lang.reflect.Type;

public class NElementMapperNRepositoryLocation implements NElementMapper<NRepositoryLocation> {

    @Override
    public Object destruct(NRepositoryLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return o.toString();
    }

    @Override
    public NElement createElement(NRepositoryLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(o.toString());
    }

    @Override
    public NRepositoryLocation createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return NRepositoryLocation.of(o.asStringValue().get());
    }

}
