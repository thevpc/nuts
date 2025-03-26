package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.util.NEnum;

import java.lang.reflect.Type;

public class NElementMapperNEnum implements NElementMapper<NEnum> {

    public NElementMapperNEnum() {
    }

    @Override
    public NEnum createObject(NElement json, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        Class cc = ReflectUtils.getRawClass(typeOfResult);
        return (NEnum) NEnum.parse(cc,json.asStringValue().get()).get();
    }

    public NElement createElement(NEnum src, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofString(src.id());
    }

    @Override
    public Object destruct(NEnum src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

}
