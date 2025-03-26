package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.format.NFormats;

import java.lang.reflect.Type;

public class NElementMapperNVersion implements NElementMapper<NVersion> {

    @Override
    public Object destruct(NVersion src, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            return NFormats.of().ofFormat(src).get().setNtf(true).format();
        } else {
            return src.toString();
        }
    }

    @Override
    public NElement createElement(NVersion o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            return context.elem().ofString(NFormats.of().ofFormat(o).get().setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NVersion createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return NVersion.get(o.asStringValue().get()).get();
    }

}
