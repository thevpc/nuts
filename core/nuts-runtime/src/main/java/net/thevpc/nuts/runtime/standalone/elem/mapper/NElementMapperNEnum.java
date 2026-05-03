package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.util.NEnum;

public class NElementMapperNEnum implements NElementMapper<NEnum> {

    public NElementMapperNEnum() {
    }

    @Override
    public NEnum toObject(NElementDeserializerContext context) {
        Class cc = NReflectUtils.getRawClass(context.instanceType()).get();
        return (NEnum) NEnum.parse(cc, context.element().asStringValue().get()).get();
    }

    public NElement toElement(NElementSerializerContext<NEnum> context) {
        return NElement.ofString(context.instance().id());
    }

    @Override
    public Object toSimple(NElementSerializerContext<NEnum> context) {
        return context.instance();
    }

}
