package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.util.NEnum;

import java.lang.reflect.Type;

public class NElementMapperNEnum implements NElementMapper<NEnum> {

    public NElementMapperNEnum() {
    }

    @Override
    public NEnum createObject(NElementDeserializerContext context) {
        Class cc = NReflectUtils.getRawClass(context.to()).get();
        return (NEnum) NEnum.parse(cc, context.element().asStringValue().get()).get();
    }

    public NElement createElement(NEnum src, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(src.id());
    }

    @Override
    public Object toSimple(NEnum src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

}
