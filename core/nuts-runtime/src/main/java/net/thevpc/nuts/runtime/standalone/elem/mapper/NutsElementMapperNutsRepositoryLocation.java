package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.spi.NutsRepositoryLocation;

import java.lang.reflect.Type;

public class NutsElementMapperNutsRepositoryLocation implements NutsElementMapper<NutsRepositoryLocation> {

    @Override
    public Object destruct(NutsRepositoryLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
        return o.toString();
    }

    @Override
    public NutsElement createElement(NutsRepositoryLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofString(o.toString());
    }

    @Override
    public NutsRepositoryLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return NutsRepositoryLocation.of(o.asString().get(session));
    }

}
