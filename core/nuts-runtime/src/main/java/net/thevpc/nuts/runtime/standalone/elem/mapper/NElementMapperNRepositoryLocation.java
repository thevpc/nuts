package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.spi.NRepositoryLocation;

import java.lang.reflect.Type;

public class NElementMapperNRepositoryLocation implements NElementMapper<NRepositoryLocation> {

    @Override
    public Object destruct(NRepositoryLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return o.toString();
    }

    @Override
    public NElement createElement(NRepositoryLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofString(o.toString());
    }

    @Override
    public NRepositoryLocation createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        return NRepositoryLocation.of(o.asString().get());
    }

}
