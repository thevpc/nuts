package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;

import java.lang.reflect.Type;

public class NutsElementMapperNutsEnum implements NutsElementMapper<NutsEnum> {

    public NutsElementMapperNutsEnum() {
    }

    @Override
    public NutsEnum createObject(NutsElement json, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        Class cc = ReflectUtils.getRawClass(typeOfResult);
        return (NutsEnum) NutsEnum.parse(cc,json.asString().get(session)).get(session);
    }

    public NutsElement createElement(NutsEnum src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofString(src.id());
    }

    @Override
    public Object destruct(NutsEnum src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

}
